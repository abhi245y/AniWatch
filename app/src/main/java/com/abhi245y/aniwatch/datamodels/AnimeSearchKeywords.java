package com.abhi245y.aniwatch.datamodels;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AnimeSearchKeywords {
    @BsonProperty("keyword")
    public String keyword;

    @BsonCreator
    public AnimeSearchKeywords(@BsonProperty("keyword") String keyword) {
        this.keyword = keyword;
    }
}
