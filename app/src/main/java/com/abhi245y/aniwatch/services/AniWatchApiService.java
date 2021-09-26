package com.abhi245y.aniwatch.services;

import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.AniApiRecentListModel;
import com.abhi245y.aniwatch.datamodels.AniApiRetroModel;
import com.abhi245y.aniwatch.datamodels.AniApiServerStatus;
import com.abhi245y.aniwatch.datamodels.AniApiYTLinkModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AniWatchApiService {
    @GET("anime")
    Call<AniApiRetroModel> search(@Query("q") String query);

    @GET("anime/dl")
    Call<AniApiDownloadModel> videoLink(@Query("anime_id") String query, @Query("ep") String epNum, @Query("loc") String fromActivity, @Query("ep_link") String epLink);

    @GET("recent_release")
    Call<AniApiRecentListModel> getRecentReleases(@Query("domain") String domain);

    @GET("yt_video")
    Call<AniApiYTLinkModel> getYtLink(@Query("id") String domain);

    @GET("/")
    Call<AniApiServerStatus> getStatus();

}
