package com.abhi245y.aniwatch.datamodels;

import com.google.gson.Gson;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AniApiServerStatus {


    private String link;
    private String response;
    private String versionCode;

    public static AniApiServerStatus objectFromData(String str) {

        return new Gson().fromJson(str, AniApiServerStatus.class);
    }
}
