package com.abhi245y.aniwatch.ui;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abhi245y.aniwatch.Adaptors.MyListAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.MongoDBAuth;
import com.abhi245y.aniwatch.datamodels.AnimeMongo;
import com.abhi245y.aniwatch.datamodels.UserMyListMongoModel;
import com.abhi245y.aniwatch.datamodels.UsersModelMongo;

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

public class MyListFragment extends Fragment {

    RecyclerView myListRV;
    ProgressBar myListLoading;
    MyListAdaptor myListAdaptor;
    SwipeRefreshLayout refreshLayout;

    Context appContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
    }


    public MyListFragment() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_my_list, container, false);


        myListRV = root.findViewById(R.id.my_list_view);
        myListLoading = root.findViewById(R.id.my_list_loading);
        refreshLayout = root.findViewById(R.id.refreshView);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);

        App app = new MongoDBAuth().getMongoApp();
        String savedUsername= new MongoDBAuth().getUsername(appContext);
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), result -> {
            if(result.isSuccess()) {
                Log.v("MyListFragment", "successfully Logged in");
                User user = app.currentUser();
                assert user != null;
                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");
                fetchMyList(mongoDatabase, savedUsername);

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchMyList(mongoDatabase, savedUsername);
                    }
                });
            }
        });



        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchMyList(MongoDatabase mongoDatabase, String savedUsername) {
        CodecRegistry pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoCollection<UsersModelMongo> releasesMongoCollection = mongoDatabase.getCollection("users", UsersModelMongo.class).withCodecRegistry(pojoCodecRegistry);

        MongoCollection<AnimeMongo> animeMongoMongoCollection =
                mongoDatabase.getCollection("anime", AnimeMongo.class).withCodecRegistry(pojoCodecRegistry);

        releasesMongoCollection.find(new Document("username", savedUsername)).iterator().getAsync(result -> {
            if (result.isSuccess()){
                MongoCursor<UsersModelMongo> results = result.get();
                Log.v("MyListFragment", "query result got" + results);
                ArrayList<AnimeMongo> myListAdaptorArray = new ArrayList<>();
                myListAdaptor = new MyListAdaptor(myListAdaptorArray, appContext);
                myListRV.setAdapter(myListAdaptor);
                myListRV.setLayoutManager(new GridLayoutManager(appContext,3));

                while (results.hasNext()) {
                    try {
                        UsersModelMongo res = results.next();
                        for (UserMyListMongoModel userMyListMongoModel: res.getMy_list()){
                            Document animeQuery = new Document("gogoVcID", userMyListMongoModel.getGogoVcID());

                            animeMongoMongoCollection.find(animeQuery).first().getAsync(result1 -> {
                                AnimeMongo animeMongo = result1.get();
                                myListAdaptorArray.add(animeMongo);
                                myListAdaptor.notifyDataSetChanged();
                                myListLoading.setVisibility(View.GONE);

                            });
                        }
                        Log.v("MyListFragment", "successfully got. Results");
                        refreshLayout.setRefreshing(false);

                    } catch (Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);

                    }
                }

            }else {
                Log.e("MyListFragment", "failed to aggregate documents with: ",
                        result.getError());
                refreshLayout.setRefreshing(false);

            }
        });
    }
}