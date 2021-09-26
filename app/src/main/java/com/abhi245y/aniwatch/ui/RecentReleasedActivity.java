package com.abhi245y.aniwatch.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.Adaptors.RecentReleasedAniViewAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.datamodels.AniApiRecentListModel;
import com.abhi245y.aniwatch.services.AniWatchApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecentReleasedActivity extends AppCompatActivity {

    RecyclerView recentReleasesRview;
    String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
    Retrofit aniRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL_ANI_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    AniWatchApiService aniWatchApiService = aniRetrofit.create(AniWatchApiService.class);

    ProgressBar recentListLoading;
    ImageView changeDomainBtn;
    MaterialButtonToggleGroup domainBtnGroup;
    RecentReleasedAniViewAdaptor recentReleasedAniViewAdaptor;
    ArrayList<AniApiRecentListModel.RecentReleasesBean> recentReleasesBeanArrayList = new ArrayList<>();
    String CURRENT_DOMAIN = "app";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_released);

        recentReleasesRview = findViewById(R.id.recent_releases_view);
        recentListLoading = findViewById(R.id.recent_list_loading);
        changeDomainBtn = findViewById(R.id.change_domain_btn);
        domainBtnGroup = findViewById(R.id.domain_btn_group);
        MaterialButton searchBtn = findViewById(R.id.search_button);

        recentReleasedAniViewAdaptor = new RecentReleasedAniViewAdaptor(recentReleasesBeanArrayList, RecentReleasedActivity.this, CURRENT_DOMAIN);
        recentReleasesRview.setAdapter(recentReleasedAniViewAdaptor);
        recentReleasesRview.setLayoutManager(new GridLayoutManager(this,3));

        changeDomainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView headerTV = findViewById(R.id.header_text_view);
                if (headerTV.getVisibility() == View.VISIBLE){
                    headerTV.setVisibility(View.GONE);
                    domainBtnGroup.setVisibility(View.VISIBLE);
                }else {
                    headerTV.setVisibility(View.VISIBLE);
                    domainBtnGroup.setVisibility(View.GONE);
                }
            }
        });


        domainBtnGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked){
                   if (checkedId == R.id.vc_btn && !CURRENT_DOMAIN.equals("vc")){
                       changeDomainBtn.setImageResource(R.drawable.naruto_doodle);
                       CURRENT_DOMAIN = "vc";
                       Toast.makeText(RecentReleasedActivity.this, "Changed Domain to :"+CURRENT_DOMAIN, Toast.LENGTH_SHORT).show();
                       fetchRecentReleases();

                   }else if (checkedId == R.id.app_btn && !CURRENT_DOMAIN.equals("app")){
                       changeDomainBtn.setImageResource(R.drawable.doodle2);
                       CURRENT_DOMAIN = "app";
                       Toast.makeText(RecentReleasedActivity.this, "Changed Domain to :"+CURRENT_DOMAIN, Toast.LENGTH_SHORT).show();
                       fetchRecentReleases();


                   }else if (checkedId == R.id.sx_btn && !CURRENT_DOMAIN.equals("sx")){
                       changeDomainBtn.setImageResource(R.drawable.doodle3);
                       CURRENT_DOMAIN = "sx";
                       Toast.makeText(RecentReleasedActivity.this, "Changed Domain to :"+CURRENT_DOMAIN, Toast.LENGTH_SHORT).show();
                       fetchRecentReleases();


                   }else if (checkedId == R.id.pro_btn && !CURRENT_DOMAIN.equals("pro")){
                       changeDomainBtn.setImageResource(R.drawable.doodle4);
                       CURRENT_DOMAIN = "pro";
                       Toast.makeText(RecentReleasedActivity.this, "Changed Domain to :"+CURRENT_DOMAIN, Toast.LENGTH_SHORT).show();
                       fetchRecentReleases();


                   }else if (checkedId == R.id.pe_btn && !CURRENT_DOMAIN.equals("pe")){
                       changeDomainBtn.setImageResource(R.drawable.doodle5);
                       CURRENT_DOMAIN = "pe";
                       Toast.makeText(RecentReleasedActivity.this, "Changed Domain to :"+CURRENT_DOMAIN, Toast.LENGTH_SHORT).show();
                       fetchRecentReleases();

                   }
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(RecentReleasedActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });

        fetchRecentReleases();
        Toast.makeText(this, "Smash Naruto to change domain !!", Toast.LENGTH_LONG).show();
    }

    public void fetchRecentReleases(){
        recentReleasesBeanArrayList.clear();
        Call<AniApiRecentListModel> aniApiRecentListModelCall =  aniWatchApiService.getRecentReleases(CURRENT_DOMAIN);
        aniApiRecentListModelCall.enqueue(new Callback<AniApiRecentListModel>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<AniApiRecentListModel> call, Response<AniApiRecentListModel> response) {
                assert response.body() != null;
                recentListLoading.setVisibility(View.GONE);
                recentReleasesBeanArrayList.addAll(response.body().getRecent_releases());
                recentReleasedAniViewAdaptor.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<AniApiRecentListModel> call, Throwable t) {

            }
        });


    }
}