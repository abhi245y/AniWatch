package com.abhi245y.aniwatch.services;

import com.abhi245y.aniwatch.datamodels.AniDBApiUpdateRecentModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AniDBService {
    @GET("/")
    Call<AniDBApiUpdateRecentModel> updateRecent();
}
