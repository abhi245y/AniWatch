package com.abhi245y.aniwatch.ui;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.MongoDBAuth;
import com.abhi245y.aniwatch.backend.MyPreferences;
import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.AniApiTotalEpisodesCallModel;
import com.abhi245y.aniwatch.datamodels.AniApiYTLinkModel;
import com.abhi245y.aniwatch.datamodels.AnimeMongo;
import com.abhi245y.aniwatch.datamodels.KitsuApiSearchModel;
import com.abhi245y.aniwatch.datamodels.PlayerActivityDataModel;
import com.abhi245y.aniwatch.datamodels.UsersModelMongo;
import com.abhi245y.aniwatch.services.AniWatchApiService;
import com.abhi245y.aniwatch.services.KitsuApiService;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.FindIterable;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint("SetTextI18n")
public class AnimeDetailsActivity extends AppCompatActivity {

    ImageView coverImage;
    TextView showTitle, releaseYear,epsDuration, ageRating, nEpisodes, showStatus, synopsisText, rating ;
    MaterialButton playBtn, downloadBtn, ytPlayBtn, showDownloads, sortEpListBtn;
    AutoCompleteTextView epSelection;
    LinearLayout mainLay;
    ProgressBar progressCircular;
    VideoView ytVideo;
    ToggleButton playPause, addToList;
    SeekBar seekProgress;
    Handler mHandler,handler;
    ConstraintLayout controlsContainer;
    ProgressBar bufferingProgress;
    AnimeMongo animeMongo;

    View loadingView;
    public boolean isFirstTime, isReversed = false;

    LinearLayout video_lay;

    String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
    String BASE_URL_kitsu_API = "https://kitsu.io/api/edge/";
    Retrofit aniRetrofit;
    AniWatchApiService aniWatchApiService;
    String videoLink = "null";
    String animeName, totalEps, currentEp;

    double current_pos, total_duration;
    boolean isVisible = true;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_details);
        isFirstTime = MyPreferences.isFirst(AnimeDetailsActivity.this);
        animeMongo = getIntent().getParcelableExtra("animeData");
        animeName = animeMongo.getTitleName();
        currentEp = getIntent().getStringExtra("current_ep");


        loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);

        Toast.makeText(this, "got: " + animeName, Toast.LENGTH_SHORT).show();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        aniRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_ANI_API)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        aniWatchApiService = aniRetrofit.create(AniWatchApiService.class);

        coverImage = findViewById(R.id.cover_img);
        showTitle = findViewById(R.id.title_name_text);
        releaseYear = findViewById(R.id.release_year_text);
        ageRating = findViewById(R.id.age_rating);
        epsDuration = findViewById(R.id.duration_text);
        nEpisodes = findViewById(R.id.total_eps_text);
        showStatus = findViewById(R.id.status_text);
        synopsisText = findViewById(R.id.synopsis_text);
        rating = findViewById(R.id.rating);

        mainLay = findViewById(R.id.mainLay);
        progressCircular = findViewById(R.id.progress_circular);
        mainLay.setVisibility(View.GONE);
        controlsContainer = findViewById(R.id.controls_container);

        addToList = findViewById(R.id.add_to_list_btn);
        sortEpListBtn = findViewById(R.id.sort_ep_list_btn);
        ytPlayBtn = findViewById(R.id.yt_pla_btn);
        playBtn = findViewById(R.id.play_btn);
        downloadBtn = findViewById(R.id.download_btn);
        epSelection = findViewById(R.id.episode_selection_view);
        epSelection = findViewById(R.id.episode_selection_view);
        mHandler = new Handler();
        handler = new Handler();
        bufferingProgress = findViewById(R.id.buffering_progress);
        showDownloads = findViewById(R.id.show_downloads_btn);

        ytVideo = findViewById(R.id.video_view);
        seekProgress = findViewById(R.id.seek_progress);
        playPause = findViewById(R.id.play_pause_button);
        video_lay = findViewById(R.id.yt_video_lay);

        new MongoDBAuth().checkPresentOnMyList(getApplicationContext(), animeMongo, addToList);


        showTitle.setText(animeMongo.getTitleName());
        releaseYear.setText(animeMongo.getReleased());
        showStatus.setText(animeMongo.getStatus());
        synopsisText.setText(animeMongo.getSummary());
        epSelection.setText("Loading Episodes..");
        playBtn.setText("Loading..");
        downloadBtn.setText("Loading..");
        epSelection.clearFocus();

        showDownloads.setOnClickListener(view -> {

            Intent downloadsActivity = new Intent(AnimeDetailsActivity.this, DownloadsActivity.class);
            startActivity(downloadsActivity);

        });

        hideLayout();
        getTotalEpisodes(animeMongo.getGogoVcID());
        getDetailsFromKitsu(animeMongo.getTitleName());

        ytPlayBtn.setOnClickListener(view -> {
            coverImage.setVisibility(View.GONE);
            ytPlayBtn.setVisibility(View.GONE);
            video_lay.setVisibility(View.VISIBLE);
            ytVideo.start();

        });

        addToList.setOnCheckedChangeListener((compoundButton, b) -> new MongoDBAuth().addToMyList(getApplicationContext(), animeMongo, "AnimeDetailsActivity", addToList));

    }
//
//    private void checkHistory(String currentEpNum){
//        App app = new MongoDBAuth().getMongoApp();
//        String savedUsername= new MongoDBAuth().getUsername(this);
//        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), new App.Callback<User>() {
//            @Override
//            public void onResult(App.Result<User> result) {
//
//                User user = app.currentUser();
//                assert user != null;
//                String gogVcId = animeMongo.getGogoVcID();
//
//                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");
//                CodecRegistry pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
//                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));
//
//                MongoCollection<UsersModelMongo> usersModelMongoMongoCollection = mongoDatabase.getCollection("users", UsersModelMongo.class).withCodecRegistry(pojoCodecRegistry);
//
//                usersModelMongoMongoCollection.findOne(new Document("username",  savedUsername).append("history_list.gogoVcID", gogVcId)
//                        .append("history_list.currentEpNum",currentEpNum), ).getAsync(result1 -> {
//                            try {
//                                if (result1.get()!=null && result1.isSuccess()){
//
//                                    result1.get()
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                });
//
//
//            }
//        });
//    }


    private void getDetailsFromKitsu(String titleName) {
        Retrofit kitsuRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_kitsu_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        KitsuApiService kitsuApiService = kitsuRetrofit.create(KitsuApiService.class);

        Call<KitsuApiSearchModel> kitsuApiSearchModelCall = kitsuApiService.search(titleName, "1");

        kitsuApiSearchModelCall.enqueue(new Callback<KitsuApiSearchModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<KitsuApiSearchModel> call, @NonNull Response<KitsuApiSearchModel> response) {
                assert response.body() != null;
                KitsuApiSearchModel.DataBean.AttributesBean attributesBean = response.body().getData().get(0).getAttributes();
                try {
                    Glide.with(AnimeDetailsActivity.this).load(attributesBean.getCoverImage().getLarge()).placeholder(R.drawable.placeholder2).into(coverImage);

                } catch (Exception e) {
                    coverImage.setBackgroundResource(R.drawable.placeholder2);
                }

                if (attributesBean.getAgeRating().equals("PG")) {
                    ageRating.getBackground().setTint(Color.rgb(67, 158, 74));
                } else if (attributesBean.getAgeRating().equals("R")) {
                    ageRating.getBackground().setTint(Color.rgb(255, 87, 91));
                }

                if (animeMongo.getSummary().equals("")){
                    synopsisText.setText(attributesBean.getSynopsis());
                }

                rating.setText(String.valueOf(attributesBean.getAverageRating()));

                ageRating.setText(String.valueOf(attributesBean.getAgeRating()));
                epsDuration.setText(attributesBean.getEpisodeLength() + " Min/Ep");

                if (attributesBean.getYoutubeVideoId() != null) {
                    if (!attributesBean.getYoutubeVideoId().equals("")) {
                        Log.d("TAG", "ID: " + attributesBean.getYoutubeVideoId());
                        Call<AniApiYTLinkModel> aniApiYTLinkModelCall = aniWatchApiService.getYtLink(attributesBean.getYoutubeVideoId());

                        aniApiYTLinkModelCall.enqueue(new Callback<AniApiYTLinkModel>() {
                            @Override
                            public void onResponse(@NonNull Call<AniApiYTLinkModel> call, @NonNull Response<AniApiYTLinkModel> response) {
                                assert response.body() != null;
                                if (response.body().getResponse().equals("success")) {

                                    String videoLink = response.body().getVideo_link();
                                    playVideo(videoLink);
                                }

                            }
                            @Override
                            public void onFailure(@NonNull Call<AniApiYTLinkModel> call, @NonNull Throwable t) {

                            }
                        });
                    }
                }

                progressCircular.setVisibility(View.GONE);
                mainLay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<KitsuApiSearchModel> call, @NonNull Throwable t) {

            }
        });
    }

    private void getTotalEpisodes(String gogoVcID) {
        Call<AniApiTotalEpisodesCallModel> aniApiTotalEpisodesCallModelCall = aniWatchApiService.getTotalEpisodes(gogoVcID);

        aniApiTotalEpisodesCallModelCall.enqueue(new Callback<AniApiTotalEpisodesCallModel>() {
            @Override
            public void onResponse(@NonNull Call<AniApiTotalEpisodesCallModel> call, @NonNull Response<AniApiTotalEpisodesCallModel> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        totalEps = response.body().getTotal_eps();
                        if (!totalEps.equals("0")) {
                            nEpisodes.setText(totalEps + " Episodes");
                            Log.d("AnimeDetailsActivity", "totalEps: " + totalEps);
                            ArrayList<String> epsList = new ArrayList<>();
                            for (int i = 0; i < Float.parseFloat(totalEps); i++) {
                                String epsCount = "Episode " + (1 + i);
                                epsList.add(epsCount);
                            }


                            if (currentEp.equals(totalEps)) {
                                Collections.reverse(epsList);
                                ArrayAdapter<String> epsDropArrayAdaptor = new ArrayAdapter<>(AnimeDetailsActivity.this, R.layout.eps_drop_down, epsList);
                                epSelection.setAdapter(epsDropArrayAdaptor);
                            } else {
                                ArrayAdapter<String> epsDropArrayAdaptor = new ArrayAdapter<>(AnimeDetailsActivity.this, R.layout.eps_drop_down, epsList);
                                epSelection.setAdapter(epsDropArrayAdaptor);
                            }

                            epSelection.setText(epsList.get(0), false);

                            fetchDownloadLink(currentEp.replace("Episode ", ""));
                            epSelection.setOnItemClickListener((adapterView, view, i, l) ->
                                    fetchDownloadLink(adapterView.getItemAtPosition(i).toString().replace("Episode ", "")));
                            loadingView.setVisibility(View.VISIBLE);

                            sortEpListBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!isReversed) {
                                        Collections.reverse(epsList);
                                        Log.v("AnimeDetailsActivity", "Sorted List: " + epsList);
                                        ArrayAdapter<String> epsDropArrayAdaptor = new ArrayAdapter<>(AnimeDetailsActivity.this, R.layout.eps_drop_down, epsList);
                                        epSelection.setAdapter(epsDropArrayAdaptor);
                                        epSelection.setText(epsList.get(0), false);
                                        isReversed = true;
                                    } else {
                                        epsList.clear();
                                        for (int i = 0; i < Float.parseFloat(totalEps); i++) {
                                            String epsCount = "Episode " + (1 + i);
                                            epsList.add(epsCount);
                                        }
                                        ArrayAdapter<String> epsDropArrayAdaptor = new ArrayAdapter<>(AnimeDetailsActivity.this, R.layout.eps_drop_down, epsList);
                                        epSelection.setAdapter(epsDropArrayAdaptor);
                                        epSelection.setText(epsList.get(0), false);
                                        isReversed = false;
                                        Log.v("AnimeDetailsActivity", "Sorted List: " + epsList);
                                    }
                                    if (!playBtn.getText().toString().replace("Play EP ", "").equals(epsList.get(0))) {
                                        fetchDownloadLink(epsList.get(0).replace("Episode ", ""));
                                    }
                                }
                            });

                        }else {
                            epSelection.setText("Coming Soon!!");
                            epSelection.setClickable(false);
                            playBtn.setText("Coming Soon!!");
                            downloadBtn.setText("Coming Soon!!");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AniApiTotalEpisodesCallModel> call, @NonNull Throwable t) {

            }
        });
    }

    private void playVideo(String videoUrl){
        ytVideo.setVideoURI(Uri.parse(videoUrl));
        ytPlayBtn.setVisibility(View.VISIBLE);


        Toast.makeText(AnimeDetailsActivity.this, "yt view preparing", Toast.LENGTH_SHORT).show();


        ytVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                playPause.setChecked(true);
                mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                    if (percent<seekProgress.getMax()) {
                        seekProgress.setSecondaryProgress(percent/100);
                    }
                });

                mediaPlayer.setOnInfoListener((mediaPlayer1, i, i1) -> {
                    switch (i) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                            bufferingProgress.setVisibility(View.GONE);
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                            bufferingProgress.setVisibility(View.VISIBLE);
                            playPause.setVisibility(View.GONE);
                            return true;
                        }
                    }
                    return false;
                });

                Toast.makeText(AnimeDetailsActivity.this, "yt view prepared video Duration: "+mediaPlayer.getDuration(), Toast.LENGTH_SHORT).show();

                current_pos = ytVideo.getCurrentPosition();
                total_duration = ytVideo.getDuration();

                seekProgress.setMax((int) total_duration);


                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            current_pos = ytVideo.getCurrentPosition();
                            seekProgress.setProgress((int) current_pos);
                            handler.postDelayed(this, 1000);
                        } catch (IllegalStateException ed){
                            ed.printStackTrace();
                        }
                    }
                };
                handler.postDelayed(runnable, 10);

                playPause.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        ytVideo.start();
                    } else {
                        ytVideo.pause();
                    }

                });

                seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        current_pos = seekBar.getProgress();
                        ytVideo.seekTo((int) current_pos);
                    }
                });
            }
        });

        ytVideo.setOnErrorListener((mediaPlayer, i, i1) -> {
            Toast.makeText(AnimeDetailsActivity.this, "Trailer Cannot be played ", Toast.LENGTH_SHORT).show();
            bufferingProgress.setVisibility(View.GONE);
            return false;
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        ytVideo.stopPlayback();
    }

    private void fetchDownloadLink(String targetEp) {
        Log.d("TAG","anime_id: "+animeMongo.getGogoVcID());

        playBtn.setText("Loading..");
        downloadBtn.setText("Loading..");

        downloadBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.cardview_dark_background));
        playBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.cardview_dark_background));
        Call<AniApiDownloadModel> aniApiDownloadModelCall = aniWatchApiService.getVideoLink(animeMongo.getGogoVcID(), targetEp);

        aniApiDownloadModelCall.enqueue(new Callback<AniApiDownloadModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<AniApiDownloadModel> call, @NonNull Response<AniApiDownloadModel> response) {
                AniApiDownloadModel result = response.body();

                try {
                    if (result != null) {
                        Log.d("TAG", "Link: " + result.getDl_link());

                        if (result.getDl_link().contains("/app/fetch_dl_link.sh: 16: printf: usage: printf format [arg ...]\\n")
                                || result.getDl_link().contains("/app/fetch_dl_link.sh:") || result.getDl_link().equals("n/a")) {
                            Log.d("TAG", "Error Getting Link id: " + animeMongo.getGogoVcID());
                            Toast.makeText(AnimeDetailsActivity.this, "Error Getting Link", Toast.LENGTH_SHORT).show();
                            downloadBtn.setClickable(false);
                            playBtn.setClickable(false);
                            downloadBtn.setText("No Link Found");
                            playBtn.setText("No Link Found");
                        }else if (result.getDl_link().contains("https://streamsb.com")){
                            Toast.makeText(AnimeDetailsActivity.this, "Trying again", Toast.LENGTH_SHORT).show();
                            downloadBtn.setClickable(false);
                            playBtn.setClickable(false);
                            fetchDownloadLink(targetEp);
                        }else {
                            videoLink = result.getDl_link();
                            Log.d("TAG", "videoLink: " + videoLink);

                            if (!videoLink.equals("null")) {
                                if (videoLink.contains("https://storage.cloud.google.com")) {
                                    Toast.makeText(AnimeDetailsActivity.this, "Alt link Found please contact me with anime name", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("TAG", "Video URL: " + videoLink);
                                    activateBtn(videoLink, targetEp);
                                }
                            }
                        }
                    }else {
                        Toast.makeText(AnimeDetailsActivity.this, "Error Getting Link", Toast.LENGTH_SHORT).show();
                        downloadBtn.setClickable(false);
                        playBtn.setClickable(false);
                        downloadBtn.setText("No Link Found");
                        playBtn.setText("No Link Found");
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(AnimeDetailsActivity.this, "Error Getting Link", Toast.LENGTH_SHORT).show();
                    downloadBtn.setClickable(false);
                    playBtn.setClickable(false);
                    downloadBtn.setText("No Link Found");
                    playBtn.setText("No Link Found");


                }
            }

            @Override
            public void onFailure(@NonNull Call<AniApiDownloadModel> call, @NonNull Throwable t) {

            }
        });
    }

    private void activateBtn(String download_link,String targetEp) {
        playBtn.setClickable(true);
        downloadBtn.setClickable(true);

        playBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.purple_200));
        downloadBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.purple_200));

        playBtn.setText("Play EP "+targetEp);
        downloadBtn.setText("Download EP "+targetEp);

        playBtn.setOnClickListener(view -> {

            PlayerActivityDataModel playerActivityDataModel = new PlayerActivityDataModel(animeMongo.getGogoVcID(),
                    download_link, targetEp, totalEps, animeMongo.getTitleName(), 0);
            Intent playerActivity = new Intent(AnimeDetailsActivity.this, VideoPlayerActivity.class);
            playerActivity.putExtra("playerData", playerActivityDataModel);
            startActivity(playerActivity);

        });

        downloadBtn.setOnClickListener(view -> {
            if(!videoLink.equals("null")) {
                if(checkPermission()){
                    setDownload();
                }else{
                    requestPermission();
                }
            }
        });
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(AnimeDetailsActivity.this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(AnimeDetailsActivity.this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            ActivityCompat.requestPermissions(AnimeDetailsActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    recreate();
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0) {
                boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    recreate();
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    void setDownload(){

        Fetch fetch = Fetch.Impl.getDefaultInstance();
        String fileName = showTitle.getText().toString()+downloadBtn.getText().toString().replace("Download "," ")+".mp4";

        Request request = new Request(videoLink, "/storage/emulated/0/AniWatch/" + showTitle.getText() + "/" + fileName);
        request.setNetworkType(NetworkType.ALL);
        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");

        fetch.enqueue(request, updatedRequest -> {
            //Request was successfully enqueued for download.
            Toast.makeText(AnimeDetailsActivity.this, "Successfully Added to Downloads", Toast.LENGTH_SHORT).show();
        }, error -> {
            //An error occurred enqueuing the request.
            Toast.makeText(AnimeDetailsActivity.this, "Error Adding to Downloads", Toast.LENGTH_SHORT).show();
            Log.d("Download","Error "+ error.getHttpResponse()+" "+error.getThrowable());
        });
    }


    public void hideLayout() {

        final Runnable runnable = () -> {
            seekProgress.setVisibility(View.GONE);
            playPause.setVisibility(View.GONE);
            isVisible = false;
        };
        handler.postDelayed(runnable, 5000);

        controlsContainer.setOnClickListener(v -> {
            mHandler.removeCallbacks(runnable);
            if (isVisible) {
                seekProgress.setVisibility(View.GONE);
                playPause.setVisibility(View.GONE);
                isVisible = false;
            } else {
                seekProgress.setVisibility(View.VISIBLE);
                if (bufferingProgress.getVisibility() != View.VISIBLE) {
                    playPause.setVisibility(View.VISIBLE);
                }
                mHandler.postDelayed(runnable, 5000);
                isVisible = true;
            }
        });

    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        ytVideo.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ytVideo.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ytVideo.pause();
    }
}