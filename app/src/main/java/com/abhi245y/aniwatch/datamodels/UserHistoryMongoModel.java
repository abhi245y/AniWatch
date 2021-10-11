package com.abhi245y.aniwatch.datamodels;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;

@Data
public class UserHistoryMongoModel {

    @BsonProperty("currentEpNum") public String currentEpNum;
    @BsonProperty("currentEpTitle") public String currentEpTitle;
    @BsonProperty("gogoVcID") public String gogoVcID;
    @BsonProperty("lastPlaybackPos") public long lastPlaybackPos;
    @BsonProperty("thumpImageUri") public String thumpImageUri;
    @BsonProperty("titleName") public String titleName;
    @BsonProperty("totalEpisodes") public String totalEpisodes;
    @BsonProperty("totalVidDuration") public long totalVidDuration;
    @BsonProperty("lastupated") public String lastupated;

    @BsonCreator
    public UserHistoryMongoModel(
            @BsonProperty("currentEpNum")  String currentEpNum,
            @BsonProperty("currentEpTitle")  String currentEpTitle,
            @BsonProperty("gogoVcID")  String gogoVcID,
            @BsonProperty("lastPlaybackPos")  long lastPlaybackPos,
            @BsonProperty("thumpImageUri")  String thumpImageUri,
            @BsonProperty("titleName")  String titleName,
            @BsonProperty("totalEpisodes")  String totalEpisodes,
            @BsonProperty("totalVidDuration")  long totalVidDuration,
            @BsonProperty("lastupated") String lastupated) {
        this.currentEpNum = currentEpNum;
        this.currentEpTitle = currentEpTitle;
        this.gogoVcID = gogoVcID;
        this.lastPlaybackPos = lastPlaybackPos;
        this.thumpImageUri = thumpImageUri;
        this.titleName = titleName;
        this.totalEpisodes = totalEpisodes;
        this.totalVidDuration = totalVidDuration;
        this.lastupated = lastupated;
    }
}
