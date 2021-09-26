package com.abhi245y.aniwatch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.Adaptors.SearchResultAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.datamodels.AniApiRetroModel;
import com.abhi245y.aniwatch.datamodels.SearchViewModel;
import com.abhi245y.aniwatch.services.AniWatchApiService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity {

    String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
    Retrofit retrofit;
    TextView noResText;
    ImageView noResImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        EditText searchEdit = findViewById(R.id.search_edit);
        RecyclerView searchResultView = findViewById(R.id.search_result_view);
        ConstraintLayout noResultLay = findViewById(R.id.no_res_lay);
        ProgressBar searchProgress = findViewById(R.id.search_loading);
        noResImg = findViewById(R.id.no_res_img);
        noResText = findViewById(R.id.no_res_text);
        searchProgress.setIndeterminate(false);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_ANI_API)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AniWatchApiService aniWatchApiService = retrofit.create(AniWatchApiService.class);
        ArrayList<SearchViewModel> searchViewModelArrayList = new ArrayList<>() ;
        SearchResultAdaptor searchResultAdaptor = new SearchResultAdaptor(searchViewModelArrayList, SearchActivity.this);
        searchResultView.setAdapter(searchResultAdaptor);
        searchResultView.setLayoutManager(new LinearLayoutManager(this));

        searchEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH){
                    Log.d("TAG","started Search");
                    searchProgress.setVisibility(View.VISIBLE);
                    noResultLay.setVisibility(View.GONE);
                    searchViewModelArrayList.clear();
                    CountDownTimer searchTimer =   new CountDownTimer(30000,1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            if(millisUntilFinished/1000 == 25){
                                Toast.makeText(SearchActivity.this, "Hmmm this is taking longer than expected", Toast.LENGTH_LONG).show();
                            }if(millisUntilFinished/1000 == 20){
                                Toast.makeText(SearchActivity.this, "Ok I know you impatient, give me 5 more sec ", Toast.LENGTH_LONG).show();
                            }if(millisUntilFinished/1000 == 15){
                                Toast.makeText(SearchActivity.this, "Ya ya I know It has been long, you can wait if you want, 15 more sec and it will be over I sware", Toast.LENGTH_LONG).show();
                            }if(millisUntilFinished/1000 == 2){
                                Toast.makeText(SearchActivity.this, "I appreciate your patience you will get your result in 2 sec", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFinish() {
                            Toast.makeText(SearchActivity.this, "Kaboom did you get it", Toast.LENGTH_SHORT).show();
                        }
                    }.start();

                    InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    searchResultView.requestFocus();
                    Toast.makeText(SearchActivity.this, "Searching: "+searchEdit.getText().toString(), Toast.LENGTH_SHORT).show();
                    Call<AniApiRetroModel> aniApiRetroModelCall = aniWatchApiService.search(searchEdit.getText().toString());
                    aniApiRetroModelCall.enqueue(new Callback<AniApiRetroModel>() {
                        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                        @Override
                        public void onResponse(Call<AniApiRetroModel> call, Response<AniApiRetroModel> response) {
                            if(response.body() != null) {
                                if (response.body().getSearch_result().size() == 0) {
                                    searchResultView.setVisibility(View.GONE);
                                    noResImg.setImageResource(R.drawable.no_res2);
                                    noResText.setText("Anime Not Found");
                                    Toast.makeText(SearchActivity.this, "No result found try again with different name", Toast.LENGTH_LONG).show();
                                    noResultLay.setVisibility(View.VISIBLE);
                                } else {
                                    for (AniApiRetroModel.SearchResultBean res : response.body().getSearch_result()) {
                                        SearchViewModel searchViewModel = new SearchViewModel(res.getAnime_name(), res.getGogo_id(), res.getPoster_link(), res.getTotal_ep());
                                        searchViewModelArrayList.add(searchViewModel);
                                    }
                                    searchResultAdaptor.notifyDataSetChanged();
                                    searchResultView.setVisibility(View.VISIBLE);
                                    noResultLay.setVisibility(View.GONE);
                                }
                                Toast.makeText(SearchActivity.this, "Kaboom", Toast.LENGTH_SHORT).show();
                                searchProgress.setProgress(100);
                            }else {
                                searchResultView.setVisibility(View.GONE);
                                noResImg.setImageResource(R.drawable.no_res2);
                                noResText.setText("Anime Not Found!");
                                Toast.makeText(SearchActivity.this, "No result found try again with different name", Toast.LENGTH_LONG).show();
                                noResultLay.setVisibility(View.VISIBLE);
                            }
                            searchTimer.cancel();
                            searchProgress.setVisibility(View.GONE);

                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFailure(Call<AniApiRetroModel> call, Throwable t) {
                            searchTimer.cancel();
                            Toast.makeText(SearchActivity.this, "Oh Bummer something went wrong", Toast.LENGTH_SHORT).show();
                            searchProgress.setVisibility(View.GONE);
                            searchResultView.setVisibility(View.GONE);
                            noResImg.setImageResource(R.drawable.server_error);
                            noResText.setText("Oh Bummer something went wrong");
                            noResultLay.setVisibility(View.VISIBLE);
                        }
                    });

                    return true;

                }
                return false;
            }
        });

    }
}

