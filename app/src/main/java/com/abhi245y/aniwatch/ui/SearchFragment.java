package com.abhi245y.aniwatch.ui;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abhi245y.aniwatch.Adaptors.SearchResultAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.MongoDBAuth;
import com.abhi245y.aniwatch.datamodels.AnimeMongo;
import com.abhi245y.aniwatch.datamodels.SearchViewModel;
import com.google.android.material.textfield.TextInputEditText;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.AggregateIterable;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class SearchFragment extends Fragment {

    TextView noResText;
    ImageView noResImg;

    MongoDatabase mongoDatabase;
    ArrayList<SearchViewModel> searchViewModelArrayList = new ArrayList<>();
    SearchResultAdaptor searchResultAdaptor;
    ConstraintLayout noResultLay;
    SwipeRefreshLayout refreshLayout;


    Context appContext;


    public SearchFragment(){}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);
        TextInputEditText searchEdit = root.findViewById(R.id.search_edit);
        RecyclerView searchResultView = root.findViewById(R.id.search_result_view);
        noResultLay = root.findViewById(R.id.no_res_lay);
        ProgressBar searchProgress = root.findViewById(R.id.search_loading);
        noResImg = root.findViewById(R.id.no_res_img);
        noResText = root.findViewById(R.id.no_res_text);
        searchProgress.setIndeterminate(false);
        refreshLayout = root.findViewById(R.id.refreshView);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);

        searchResultAdaptor = new SearchResultAdaptor(searchViewModelArrayList, appContext);
        searchResultView.setAdapter(searchResultAdaptor);
        searchResultView.setLayoutManager(new LinearLayoutManager(appContext));

        App app = new MongoDBAuth().getMongoApp();
        String savedUsername= new MongoDBAuth().getUsername(appContext);
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), result -> {
            if(result.isSuccess()) {
                User user = app.currentUser();
                assert user != null;
                mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");
                searchEdit.setVisibility(View.VISIBLE);
            }
        });


        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("SearchFragment", "Count i1 "+i1+" and i "+i+" and i2 "+i2);
                if (i1>=2 || i>=2) {
                    Log.d("SearchFragment", "Searching "+charSequence);
                    getSearchRes(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getSearchRes(String charSequence) {
        refreshLayout.setRefreshing(true);
        CodecRegistry pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoCollection<AnimeMongo> mongoCollection = mongoDatabase.getCollection("anime", AnimeMongo.class).withCodecRegistry(pojoCodecRegistry);

        List<Document> queryFilter = Arrays.asList(new Document("$search",
                        new Document("index", "animeSearch")
                                .append("text",
                                        new Document("query", charSequence)
                                                .append("path",
                                                        new Document("wildcard", "*")))),
                new Document("$limit", 20L));


        AggregateIterable<AnimeMongo> aggregateIterable = mongoCollection.aggregate(queryFilter, AnimeMongo.class);
        Log.d("SearchFragment", "aggregateIterable: " + aggregateIterable);

        RealmResultTask<MongoCursor<AnimeMongo>> aggregationTask  = aggregateIterable.iterator();

        aggregationTask.getAsync(task -> {
            if (task.isSuccess()) {
                noResultLay.setVisibility(View.GONE);
                MongoCursor<AnimeMongo> results = task.get();
                searchViewModelArrayList.clear();

                while (results.hasNext()) {
                    try {
                        AnimeMongo res = results.next();
                        searchViewModelArrayList.add(new SearchViewModel(res));
                        Log.v("SearchFragment", "successfully aggregated the anime. Results:" +res.getGenre());
                    } catch (Exception e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);

                    }

                }
                refreshLayout.setRefreshing(false);
                searchResultAdaptor.notifyDataSetChanged();

            } else {
                Toast.makeText(appContext, "Oh Bummer something went wrong", Toast.LENGTH_SHORT).show();
/*                noResImg.setImageResource(R.drawable.server_error);
                String serverError = "Oh Bummer something went wrong";
                noResText.setText(serverError);
                noResultLay.setVisibility(View.VISIBLE);
 */
                Log.e("SearchFragment", "failed to aggregate documents with: ",
                        task.getError());
                refreshLayout.setRefreshing(false);
            }
        });
    }
}

