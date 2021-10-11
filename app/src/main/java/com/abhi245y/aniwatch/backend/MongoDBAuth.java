package com.abhi245y.aniwatch.backend;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.abhi245y.aniwatch.datamodels.AnimeMongo;

import org.bson.Document;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions;
import io.realm.mongodb.mongo.result.UpdateResult;
import io.realm.mongodb.sync.SyncSession;

public class MongoDBAuth {

    public App getMongoApp(){
        String appID = "aniwatch-realm-test-hfisf";
        SyncSession.ClientResetHandler handler = (session, error) -> Log.e("SearchFragment", "Client Reset required for: " +
                session.getConfiguration().getServerUrl() + " for error: " +
                error.toString());

        return new App(new AppConfiguration.Builder(appID)
                .defaultClientResetHandler(handler)
                .build());
    }

    public String getUsername(Context context){
        SharedPreferences sp1=context.getSharedPreferences("Login", MODE_PRIVATE);
        return sp1.getString("username",null);
    }

    public void addToMyList(Context context, AnimeMongo animeMongo, String tag, ToggleButton addToList){
        App app = getMongoApp();
        String username = getUsername(context);
        String gogoVcID = animeMongo.getGogoVcID();
        String titleName = animeMongo.getTitleName();
        String imgLink = animeMongo.getImgLink();
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", username)), new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                User user = app.currentUser();
                assert user != null;

                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");

                Document userListData = new Document("gogoVcID", gogoVcID).append("titleName", titleName).append("imgLink", imgLink);

                if (addToList.isChecked()) {
                    mongoDatabase.getCollection("users").findOne(new Document("username", username).append("my_list.gogoVcID", gogoVcID)).getAsync(new App.Callback<Document>() {
                        @Override
                        public void onResult(App.Result<Document> result1) {
                            try {
                                if (result1.get() == null) {
                                    mongoDatabase.getCollection("users")
                                            .findOneAndUpdate(new Document("username", username), new Document("$push", new Document("my_list", userListData)),
                                                    new FindOneAndModifyOptions().returnNewDocument(true)).getAsync(result3 -> {
                                        if (result3.isSuccess()) {

                                            Log.v(tag, "successfully data added to user list: " + result3.get());

                                        } else {
                                            Log.e(tag, "Error data adding to user list " + result3.getError());
                                        }
                                    });

                                    Toast.makeText(context, "Added to my list", Toast.LENGTH_SHORT).show();

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }else {
                    Log.v(tag, "List date to remove " + userListData);
                    mongoDatabase.getCollection("users").updateOne(new Document("username", username).append("my_list.gogoVcID", gogoVcID),
                            new Document("$pull", new Document("my_list", userListData))).getAsync(new App.Callback<UpdateResult>() {
                        @Override
                        public void onResult(App.Result<UpdateResult> result) {
                            if (result.get()!=null && result.isSuccess() ){
                                Toast.makeText(context, " Removed from my list", Toast.LENGTH_SHORT).show();
                            }else {
                                Log.v(tag, "List date remove error " + result.get());
                                addToList.setChecked(true);
                            }
                        }
                    });
                }

            }
        });
    }

    public void checkPresentOnMyList(Context context, AnimeMongo animeMongo, ToggleButton addToList){
        App app = getMongoApp();
        String username = getUsername(context);
        String gogoVcID = animeMongo.getGogoVcID();

        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", username)), new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                User user = app.currentUser();
                assert user != null;

                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");

                mongoDatabase.getCollection("users").findOne(new Document("username", username).append("my_list.gogoVcID", gogoVcID)).getAsync(new App.Callback<Document>() {
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if (result.isSuccess() && result.get()!=null){
                            addToList.setChecked(true);
                        }else {
                            addToList.setChecked(false);
                        }
                    }
                });
            }
        });
    }

}
