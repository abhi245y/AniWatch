package com.abhi245y.aniwatch.datamodels;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;

@Data
public class UserMyListMongoModel {
    @BsonProperty("gogoVcID") public String gogoVcID;
    @BsonProperty("imgLink") public String imgLink;
    @BsonProperty("titleName") public String titleName;


    @BsonCreator
    public UserMyListMongoModel(@BsonProperty("gogoVcID")  String gogoVcID,
            @BsonProperty("imgLink")  String imgLink,
            @BsonProperty("titleName")  String titleName) {
        this.gogoVcID = gogoVcID;
        this.imgLink = imgLink;
        this.titleName = titleName;
    }
}
