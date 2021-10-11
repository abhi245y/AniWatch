package com.abhi245y.aniwatch.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;

import lombok.Data;

@Data
public class AnimeMongo implements Parcelable {
    @BsonProperty("_id") public ObjectId _id;
    @BsonProperty("genre") public  ArrayList<String> genre;
    @BsonProperty("gogoVcID") public  String gogoVcID;
    @BsonProperty("id") public  String id;
    @BsonProperty("imgLink") public  String imgLink;
    @BsonProperty("otherNames") public  String otherNames;
    @BsonProperty("otherNamesList") public  ArrayList<String> otherNamesList;
    @BsonProperty("released") public  String released;
    @BsonProperty("search_keywords") public  ArrayList<AnimeSearchKeywords> search_keywords;
    @BsonProperty("season") public  String season;
    @BsonProperty("status") public  String status;
    @BsonProperty("summary") public  String summary;
    @BsonProperty("titleName") public  String titleName;

    @BsonCreator
    public AnimeMongo(@BsonProperty("_id") ObjectId _id,
                      @BsonProperty("genre") ArrayList<String> genre, @BsonProperty("gogoVcID") String gogoVcID,
                      @BsonProperty("id") String id, @BsonProperty("imgLink") String imgLink,
                      @BsonProperty("otherNames") String otherNames, @BsonProperty("otherNamesList") ArrayList<String> otherNamesList,
                      @BsonProperty("released") String released, @BsonProperty("search_keywords") ArrayList<AnimeSearchKeywords> search_keywords,
                      @BsonProperty("season") String season, @BsonProperty("status") String status,
                      @BsonProperty("summary") String summary, @BsonProperty("titleName") String titleName) {
        this._id = _id;
        this.genre = genre;
        this.gogoVcID = gogoVcID;
        this.id = id;
        this.imgLink = imgLink;
        this.otherNames = otherNames;
        this.otherNamesList = otherNamesList;
        this.released = released;
        this.search_keywords = search_keywords;
        this.season = season;
        this.status = status;
        this.summary = summary;
        this.titleName = titleName;
    }

    protected AnimeMongo(Parcel in) {
        genre = in.createStringArrayList();
        gogoVcID = in.readString();
        id = in.readString();
        imgLink = in.readString();
        otherNames = in.readString();
        otherNamesList = in.createStringArrayList();
        released = in.readString();
        season = in.readString();
        status = in.readString();
        summary = in.readString();
        titleName = in.readString();
    }

    public static final Creator<AnimeMongo> CREATOR = new Creator<AnimeMongo>() {
        @Override
        public AnimeMongo createFromParcel(Parcel in) {
            return new AnimeMongo(in);
        }

        @Override
        public AnimeMongo[] newArray(int size) {
            return new AnimeMongo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(genre);
        parcel.writeString(gogoVcID);
        parcel.writeString(id);
        parcel.writeString(imgLink);
        parcel.writeString(otherNames);
        parcel.writeStringList(otherNamesList);
        parcel.writeString(released);
        parcel.writeString(season);
        parcel.writeString(status);
        parcel.writeString(summary);
        parcel.writeString(titleName);
    }
}


