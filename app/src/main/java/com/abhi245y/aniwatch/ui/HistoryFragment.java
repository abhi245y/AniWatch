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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abhi245y.aniwatch.Adaptors.HistoryListAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.MongoDBAuth;
import com.abhi245y.aniwatch.datamodels.UserHistoryMongoModel;
import com.abhi245y.aniwatch.datamodels.UsersModelMongo;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class HistoryFragment extends Fragment {

    TextView dialogue;
    ImageView emptyHistoryImg;
    ProgressBar progressBar;
    RecyclerView historyRecyclerView;
    SwipeRefreshLayout refreshLayout;




    public HistoryFragment() { }

    Context appContext;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);

        dialogue = root.findViewById(R.id.dialogue_tv);
        emptyHistoryImg = root.findViewById(R.id.empty_history_img);
        progressBar = root.findViewById(R.id.loading_pb);
        historyRecyclerView = root.findViewById(R.id.history_rv);
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
                fetchHistoryList(mongoDatabase, savedUsername);

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchHistoryList(mongoDatabase, savedUsername);
                    }
                });
            }
        });

//        new CountDownTimer(5000,1000) {
//
//            @Override
//            public void onTick(long millisUntilFinished) { }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onFinish() {
//                Glide.with(appContext).asGif().load(R.drawable.go_watch).into(emptyHistoryImg);
//                dialogue.setText("Go Watch Some Anime.. Bakka!!");
//            }
//        }.start();

        return root;


    }

    private void fetchHistoryList(MongoDatabase mongoDatabase, String savedUsername) {

        CodecRegistry pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoCollection<UsersModelMongo> usersModelMongoMongoCollection = mongoDatabase.getCollection("users", UsersModelMongo.class).withCodecRegistry(pojoCodecRegistry);


        usersModelMongoMongoCollection.find(new Document("username", savedUsername)).iterator().getAsync(new App.Callback<MongoCursor<UsersModelMongo>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResult(App.Result<MongoCursor<UsersModelMongo>> result) {
                if (result.isSuccess()) {
                    MongoCursor<UsersModelMongo> results = result.get();
                    while (results.hasNext()) {
                        try {
                            UsersModelMongo res = results.next();
                            ArrayList<UserHistoryMongoModel> userHistoryMongoModels = new ArrayList<>(res.getHistory_list());

                            userHistoryMongoModels.sort((userHistoryMongoModel, t1) -> {
                                final DateFormat f = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

                                try {
                                    return Objects.requireNonNull(f.parse(t1.getLastupated())).compareTo(f.parse(userHistoryMongoModel.getLastupated()));
                                } catch (ParseException e) {
                                    throw new IllegalArgumentException(e);
                                }
                            });

                            HistoryListAdaptor historyListAdaptor = new HistoryListAdaptor(userHistoryMongoModels, appContext);
                            historyRecyclerView.setAdapter(historyListAdaptor);
                            historyRecyclerView.setLayoutManager(new LinearLayoutManager(appContext));
                            progressBar.setVisibility(View.GONE);
                            refreshLayout.setRefreshing(false);

                        } catch (Exception e) {
                            refreshLayout.setRefreshing(false);
                            e.printStackTrace();

                        }
                    }
                }else {
                    refreshLayout.setRefreshing(false);
                }
            }
        });

    }
}