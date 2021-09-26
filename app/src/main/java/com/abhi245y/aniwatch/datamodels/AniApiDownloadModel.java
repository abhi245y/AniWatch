package com.abhi245y.aniwatch.datamodels;

import com.google.gson.Gson;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AniApiDownloadModel {


    private String dl_link;

    public static AniApiDownloadModel objectFromData(String str) {

        return new Gson().fromJson(str, AniApiDownloadModel.class);
    }
}
