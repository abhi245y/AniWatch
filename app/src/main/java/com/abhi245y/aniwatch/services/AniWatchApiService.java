package com.abhi245y.aniwatch.services;

import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.AniApiRecentListModel;
import com.abhi245y.aniwatch.datamodels.AniApiRetroModel;
import com.abhi245y.aniwatch.datamodels.AniApiServerStatus;
import com.abhi245y.aniwatch.datamodels.AniApiTotalEpisodesCallModel;
import com.abhi245y.aniwatch.datamodels.AniApiYTLinkModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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

    @GET("dev/fetch_total_eps")
    Call<AniApiTotalEpisodesCallModel> getTotalEpisodes(@Query("anime_id") String gogo_id);

    @GET("dev/fetch_dl")
    Call<AniApiDownloadModel> getVideoLink(@Query("anime_id") String query, @Query("ep") String epNum);

    @Multipart
    @POST("upload_thumbnail")
    Call<ResponseBody> uploadPhoto(@Part("description") RequestBody description, @Part MultipartBody.Part file, @Query("user_id") String user_id);

}
