package com.abhi245y.aniwatch.datamodels;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;


import java.util.ArrayList;

import lombok.Data;

@Data
public class UsersModelMongo {
    @BsonProperty("_id") public ObjectId _id;
    @BsonProperty("history_list") public ArrayList<UserHistoryMongoModel> history_list;
    @BsonProperty("id") public String id;
    @BsonProperty("my_list")public ArrayList<UserMyListMongoModel> my_list;
    @BsonProperty("username")public String username;

    @BsonCreator
    public UsersModelMongo( @BsonProperty("_id")  ObjectId _id,
                              @BsonProperty("history_list")  ArrayList<UserHistoryMongoModel> history_list,
                              @BsonProperty("id")  String id,
                              @BsonProperty("my_list") ArrayList<UserMyListMongoModel> my_list,
                              @BsonProperty("username") String username) {
        this._id = _id;
        this.history_list = history_list;
        this.id = id;
        this.my_list = my_list;
        this.username = username;
    }
}
