package com.abhi245y.aniwatch.datamodels;

import com.google.gson.Gson;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AniApiYTLinkModel {


    private String  response;
    private String poster_link;
    private String title;
    private String video_link;

    public static AniApiYTLinkModel objectFromData(String str) {

        return new Gson().fromJson(str, AniApiYTLinkModel.class);
    }
}
