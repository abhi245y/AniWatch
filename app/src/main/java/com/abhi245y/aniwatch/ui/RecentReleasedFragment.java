package com.abhi245y.aniwatch.ui;

import static android.content.Context.MODE_PRIVATE;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abhi245y.aniwatch.Adaptors.RecentReleasedAniViewAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.MongoDBAuth;
import com.abhi245y.aniwatch.datamodels.AniDBApiUpdateRecentModel;
import com.abhi245y.aniwatch.datamodels.AnimeMongo;
import com.abhi245y.aniwatch.datamodels.RecentReleasedAniViewModel;
import com.abhi245y.aniwatch.datamodels.RecentReleasesMongoModel;
import com.abhi245y.aniwatch.services.AniDBService;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecentReleasedFragment extends Fragment {

    RecyclerView recentReleasesRview;
    String BASE_URL_ANI_API = "https://aniwatch-database-api.herokuapp.com/";
    Retrofit aniDBRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL_ANI_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    AniDBService aniDBService = aniDBRetrofit.create(AniDBService.class);

    ProgressBar recentListLoading;
    SwipeRefreshLayout refreshLayout;


    RecentReleasedAniViewAdaptor recentReleasedAniViewAdaptor;
    ArrayList<RecentReleasedAniViewModel> recentReleasesBeanArrayList = new ArrayList<>();
    Context appContext;

    public RecentReleasedFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recent_released, container, false);

        recentReleasesRview = root.findViewById(R.id.recent_releases_view);
        recentListLoading = root.findViewById(R.id.recent_list_loading);
        refreshLayout = root.findViewById(R.id.refreshView);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);

        recentReleasedAniViewAdaptor = new RecentReleasedAniViewAdaptor(recentReleasesBeanArrayList, appContext);
        recentReleasesRview.setAdapter(recentReleasedAniViewAdaptor);
        recentReleasesRview.setLayoutManager(new GridLayoutManager(appContext,3));

        App app = new MongoDBAuth().getMongoApp();
        String savedUsername= new MongoDBAuth().getUsername(appContext);
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), result -> {
            if(result.isSuccess()) {
                Log.v("RecentReleasedFragment", "successfully Logged in");
                User user = app.currentUser();
                assert user != null;
                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");
                fetchRecentReleases(mongoDatabase);
                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchRecentReleases(mongoDatabase);
                    }
                });
            }
        });


        try {
            SharedPreferences sp1=appContext.getSharedPreferences("Update Notice", MODE_PRIVATE);
            String notice = sp1.getString("notice",null);

            if(notice==null || notice.equals("remind again")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(appContext);

                builder.setMessage(R.string.update_notice).setTitle("Update Notice");

                builder.setPositiveButton("I understand", (dialog, id) -> {
                    SharedPreferences sp= appContext.getSharedPreferences("Update Notice", MODE_PRIVATE);
                    SharedPreferences.Editor Ed=sp.edit();
                    Ed.putString("notice","acknowledged");
                    Ed.apply();
                });

                builder.setNegativeButton("Remind me again", (dialog, id) ->{
                    SharedPreferences sp=appContext.getSharedPreferences("Update Notice", MODE_PRIVATE);
                    SharedPreferences.Editor Ed=sp.edit();
                    Ed.putString("notice","remind again");
                    Ed.apply();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else {
                Log.v("RecentReleasedFragment", "Notice Acknowledged by user: "+notice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void fetchRecentReleases(MongoDatabase mongoDatabase){
        recentReleasesBeanArrayList.clear();
        CodecRegistry pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoCollection<RecentReleasesMongoModel> releasesMongoCollection =
                mongoDatabase.getCollection("recent_releases", RecentReleasesMongoModel.class).withCodecRegistry(pojoCodecRegistry);

        MongoCollection<AnimeMongo> animeMongoMongoCollection =
                mongoDatabase.getCollection("anime", AnimeMongo.class).withCodecRegistry(pojoCodecRegistry);

        releasesMongoCollection.find().iterator().getAsync(result -> {
            if (result.isSuccess()){
                MongoCursor<RecentReleasesMongoModel> results = result.get();
                Log.v("RecentReleasedFragment", "query result got" + results);
                while (results.hasNext()) {
                    try {
                        RecentReleasesMongoModel res = results.next();
                        Document animeQuery = new Document("gogoVcID", res.getGogo_vc_id());

                        animeMongoMongoCollection.find(animeQuery).first().getAsync(result1 -> {
                            AnimeMongo animeMongo = result1.get();
                            RecentReleasedAniViewModel recentReleasedAniViewModel = new RecentReleasedAniViewModel(animeMongo, res.getEp_num());
                            recentReleasesBeanArrayList.add(recentReleasedAniViewModel);
                            recentReleasedAniViewAdaptor.notifyDataSetChanged();

                            Log.v("RecentReleasedFragment", "successfully got. Results");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);

                    }
                }
                recentListLoading.setVisibility(View.GONE);
                updateRecentReleased();
                refreshLayout.setRefreshing(false);

            }else {
                Log.e("RecentReleasedFragment", "failed to aggregate documents with: ",
                        result.getError());
                refreshLayout.setRefreshing(false);

            }
        });
    }

    private void updateRecentReleased() {
        Call<AniDBApiUpdateRecentModel> aniDBApiUpdateRecentModelCall = aniDBService.updateRecent();

        aniDBApiUpdateRecentModelCall.enqueue(new Callback<AniDBApiUpdateRecentModel>() {
            @Override
            public void onResponse(@NonNull Call<AniDBApiUpdateRecentModel> call, @NonNull Response<AniDBApiUpdateRecentModel> response) { }
            @Override
            public void onFailure(@NonNull Call<AniDBApiUpdateRecentModel> call, @NonNull Throwable t) { }
        });
    }
}