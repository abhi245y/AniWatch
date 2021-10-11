package com.abhi245y.aniwatch.datamodels;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class RecentReleasesMongoModel {
    @BsonProperty("_id")private ObjectId _id;
    @BsonProperty("ep_link")private String ep_link;
    @BsonProperty("ep_num")private String ep_num;
    @BsonProperty("gogo_vc_id")private String gogo_vc_id;
    @BsonProperty("id")private String id;
    @BsonProperty("img_link")private String img_link;
    @BsonProperty("title")private String title;

    @BsonCreator
    public RecentReleasesMongoModel(@BsonProperty("_id") ObjectId _id, @BsonProperty("ep_link") String ep_link,
                                    @BsonProperty("ep_num") String ep_num, @BsonProperty("gogo_vc_id") String gogo_vc_id,
                                    @BsonProperty("id") String id, @BsonProperty("img_link") String img_link,
                                    @BsonProperty("title") String title) {
        this._id = _id;
        this.ep_link = ep_link;
        this.ep_num = ep_num;
        this.gogo_vc_id = gogo_vc_id;
        this.id = id;
        this.img_link = img_link;
        this.title = title;
    }
}
