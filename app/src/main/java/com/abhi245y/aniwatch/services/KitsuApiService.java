package com.abhi245y.aniwatch.services;

import com.abhi245y.aniwatch.datamodels.KitsuApiSearchModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KitsuApiService {

    @GET("anime")
    Call<KitsuApiSearchModel> search(@Query("filter[text]") String query, @Query("page[limit]") String pgLmt);
}
