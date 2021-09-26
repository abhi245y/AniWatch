package com.abhi245y.aniwatch.ui;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

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
import com.abhi245y.aniwatch.backend.MyPreferences;
import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.AniApiYTLinkModel;
import com.abhi245y.aniwatch.datamodels.KitsuApiSearchModel;
import com.abhi245y.aniwatch.datamodels.PlayerActivityDataModel;
import com.abhi245y.aniwatch.services.AniWatchApiService;
import com.abhi245y.aniwatch.services.KitsuApiService;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint("SetTextI18n")
public class AnimeDetailsActivity extends AppCompatActivity {

    ImageView coverImage;
    TextView showTitle, releaseYear,epsDuration, ageRating, nEpisodes, showStatus, synopsisText ;
    MaterialButton playBtn, downloadBtn, ytPlayBtn, showDownloads;
    AutoCompleteTextView epSelection;
    LinearLayout mainLay;
    ProgressBar progressCircular;
    VideoView ytVideo;
    ToggleButton playPause;
    SeekBar seekProgress;
    Handler mHandler,handler;
    ConstraintLayout controlsContainer;
    ProgressBar bufferingProgress;

    View loadingView;
    public boolean isFirstTime;

    LinearLayout video_lay;

    String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
    Retrofit aniRetrofit;
    AniWatchApiService aniWatchApiService;
    String videoLink = "null";
    String animeName, totalEps, currentEp, fromActivity, targetEp, slug;

    double current_pos, total_duration;
    boolean isVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_details);
        isFirstTime = MyPreferences.isFirst(AnimeDetailsActivity.this);
        animeName = getIntent().getStringExtra("anime_name");
        totalEps = getIntent().getStringExtra("total_ep");
        currentEp = getIntent().getStringExtra("current_ep");
        fromActivity = getIntent().getStringExtra("activity_name");

        loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);

        Toast.makeText(this, "got: " + animeName, Toast.LENGTH_SHORT).show();

        aniRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_ANI_API)
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

        mainLay = findViewById(R.id.mainLay);
        progressCircular = findViewById(R.id.progress_circular);
        mainLay.setVisibility(View.GONE);
        controlsContainer = findViewById(R.id.controls_container);

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

        showDownloads.setOnClickListener(view -> {

            Intent downloadsActivity = new Intent(AnimeDetailsActivity.this, DownloadsActivity.class);
            startActivity(downloadsActivity);

        });

        String BASE_URL_kitsu_API = "https://kitsu.io/api/edge/";
        Retrofit kitsuRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_kitsu_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        hideLayout();

        KitsuApiService kitsuApiService = kitsuRetrofit.create(KitsuApiService.class);

        Call<KitsuApiSearchModel> kitsuApiSearchModelCall = kitsuApiService.search(animeName, "1");

        ytPlayBtn.setOnClickListener(view -> {
            coverImage.setVisibility(View.GONE);
            ytPlayBtn.setVisibility(View.GONE);
            video_lay.setVisibility(View.VISIBLE);
            ytVideo.start();

        });


        kitsuApiSearchModelCall.enqueue(new Callback<KitsuApiSearchModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<KitsuApiSearchModel> call, @NonNull Response<KitsuApiSearchModel> response) {
                assert response.body() != null;
                KitsuApiSearchModel.DataBean.AttributesBean attributesBean = response.body().getData().get(0).getAttributes();
                slug = attributesBean.getSlug();
                if(fromActivity.equals("recent_release") ) {
                    if (getIntent().getStringExtra("current_domain").equals("vc") || getIntent().getStringExtra("current_domain").equals("pe")) {
                        String gogo_id = getIntent().getStringExtra("ep_link").replace("-episode-"+currentEp,"").replace("/","");
                        getDownloadLink(gogo_id, currentEp, getIntent().getStringExtra("ep_link"));
                    }else {
                        getDownloadLink(attributesBean.getSlug(), currentEp, getIntent().getStringExtra("ep_link"));
                    }
                }else {
                    getDownloadLink(animeName, currentEp, "n/a");
                }
                try {
                    Glide.with(AnimeDetailsActivity.this).load(attributesBean.getCoverImage().getLarge()).placeholder(R.drawable.placeholder2).into(coverImage);

                } catch (Exception e) {
                    coverImage.setBackgroundResource(R.drawable.placeholder2);
                }
                if (attributesBean.getTitles().getEn_jp()!=null) {
                    showTitle.setText(String.valueOf(attributesBean.getTitles().getEn_jp()));
                }else {
                    showTitle.setText(String.valueOf(attributesBean.getTitles().getEn()));
                }
                releaseYear.setText(String.valueOf(attributesBean.getStartDate()));
                if (attributesBean.getAgeRating().equals("PG")) {
                    ageRating.getBackground().setTint(Color.rgb(67, 158, 74));
                } else if (attributesBean.getAgeRating().equals("R")) {
                    ageRating.getBackground().setTint(Color.rgb(255, 87, 91));
                }
                ageRating.setText(String.valueOf(attributesBean.getAgeRating()));
                epsDuration.setText(attributesBean.getEpisodeLength() + " Min/Ep");
                nEpisodes.setText(totalEps + " Episodes");
                showStatus.setText(String.valueOf(attributesBean.getStatus()));
                synopsisText.setText(String.valueOf(attributesBean.getSynopsis()));


                ArrayList<String> epsList = new ArrayList<>();
                for (int i = 0; i < Float.parseFloat(totalEps); i++) {
                    String epsCount = "Episode " + (1 + i);
                    epsList.add(epsCount);
                }


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

                ArrayAdapter<String> epsDropArrayAdaptor = new ArrayAdapter<>(AnimeDetailsActivity.this, R.layout.eps_drop_down, epsList);
                epSelection.setAdapter(epsDropArrayAdaptor);
                if(currentEp.equals(totalEps)) {
                    epSelection.setText("Episode " + totalEps, false);
                }

                epSelection.setOnItemClickListener((adapterView, view, i, l) -> {
                    if(fromActivity.equals("recent_release")) {
                        getDownloadLink(attributesBean.getSlug(), adapterView.getItemAtPosition(i).toString().replace("Episode ", ""), getIntent().getStringExtra("ep_link"));
                    }else {
                        getDownloadLink(animeName, adapterView.getItemAtPosition(i).toString().replace("Episode ", ""), "n/a");
                    }
                });

                progressCircular.setVisibility(View.GONE);
                mainLay.setVisibility(View.VISIBLE);


            }

            @Override
            public void onFailure(@NonNull Call<KitsuApiSearchModel> call, @NonNull Throwable t) {

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

    private void getDownloadLink(String anime_id, String epNum, String epLink) {
        Log.d("TAG","anime_id: "+anime_id);

        playBtn.setText("Loading..");
        downloadBtn.setText("Loading..");
        String currentActivity = getIntent().getStringExtra("activity_name");

        downloadBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.cardview_dark_background));
        playBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.cardview_dark_background));
        targetEp = epNum;

        if (getIntent().getStringExtra("activity_name").equals("recent_release")) {
            if (getIntent().getStringExtra("current_domain").equals("vc") || getIntent().getStringExtra("current_domain").equals("pe")) {
                String gg_id = getIntent().getStringExtra("ep_link").replace("-episode-" + currentEp, "").replace("/", "");
                fetchDownloadLink(gg_id, "search_activity", epLink);
            }else {
                fetchDownloadLink(anime_id, currentActivity, epLink);
            }
        }else {
            fetchDownloadLink(anime_id, currentActivity, epLink);
        }

    }

    private void fetchDownloadLink(String anime_id, String currentActivity, String epLink) {
        Call<AniApiDownloadModel> aniApiDownloadModelCall = aniWatchApiService.videoLink(anime_id, targetEp, currentActivity, epLink.replace("https://gogoanime.app/anime/",""));

        aniApiDownloadModelCall.enqueue(new Callback<AniApiDownloadModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<AniApiDownloadModel> call, @NonNull Response<AniApiDownloadModel> response) {
                AniApiDownloadModel result = response.body();
                assert result != null;
                if(result.getDl_link().contains("/app/fetch_dl_link.sh: 16: printf: usage: printf format [arg ...]\\n")
                        || result.getDl_link().contains("/app/fetch_dl_link.sh:") || result.getDl_link().equals("n/a")) {
                    Log.d("TAG", "Error Getting Link search id: "+animeName);
                    Toast.makeText(AnimeDetailsActivity.this, "Error Getting Link", Toast.LENGTH_SHORT).show();
                    downloadBtn.setClickable(false);
                    playBtn.setClickable(false);
                    downloadBtn.setText("No Link Found");
                    playBtn.setText("No Link Found");
                }else {
                    videoLink = result.getDl_link();
                    Log.d("TAG", "videoLink: " + videoLink);

                    if (!videoLink.equals("null")) {
                        if (videoLink.contains("https://storage.cloud.google.com")) {
                            Toast.makeText(AnimeDetailsActivity.this, "Alt link Found please contact me with anime name", Toast.LENGTH_SHORT).show();
                        } else if (videoLink.contains("https://storage.googleapis.com")) {
                            Log.d("TAG", "alt URL: " + videoLink);
                            activateBtn(videoLink, epLink);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AniApiDownloadModel> call, @NonNull Throwable t) {

            }
        });
    }



    private void activateBtn(String download_link, String epLink) {
        playBtn.setClickable(true);
        downloadBtn.setClickable(true);

        playBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.purple_200));
        downloadBtn.setBackgroundTintList(ContextCompat.getColorStateList(AnimeDetailsActivity.this, R.color.purple_200));

        playBtn.setText("Play EP "+targetEp);
        downloadBtn.setText("Download EP "+targetEp);

        playBtn.setOnClickListener(view -> {
            String anime_id;
            if(fromActivity.equals("recent_release")){
                anime_id = slug;
            }else {
                anime_id = animeName;
            }
            if (anime_id != null) {
                PlayerActivityDataModel playerActivityDataModel = new PlayerActivityDataModel(anime_id, download_link, targetEp, totalEps, fromActivity, epLink);
                Intent playerActivity = new Intent(AnimeDetailsActivity.this, VideoPlayerActivity.class);
                playerActivity.putExtra("playerData", playerActivityDataModel);
                startActivity(playerActivity);
            }else {
                Toast.makeText(AnimeDetailsActivity.this, "Debug anime_id is null Playing in alt player", Toast.LENGTH_SHORT).show();

                //                boolean vlcFound = isAppInstalled(AnimeDetailsActivity.this, "org.videolan.vlc");
//                if (vlcFound) {
//
//                    Log.d("LOG","Video Link: "+videoLink);
//                    try {
//                        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
//                        vlcIntent.setPackage("org.videolan.vlc");
//                        vlcIntent.setDataAndTypeAndNormalize(download_link, "video/*");
//                        vlcIntent.putExtra("title", animeName.replace("-", " ") + " Ep " + targetEp);
//                        vlcIntent.putExtra("from_start", false);
//                        vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
//                        startActivity(vlcIntent);
//                    }catch (Exception e){
//                        Intent intent = new Intent(Intent.ACTION_VIEW,download_link);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.setPackage("com.android.chrome");
//                        try {
//                            startActivity(intent);
//                        } catch (ActivityNotFoundException ex) {
//                            intent.setPackage("org.mozilla.firefox");
//                            startActivity(intent);
//                        }
//                    }
//                }
//                else {
//                    Intent playVideo = new Intent(Intent.ACTION_VIEW);
//                    playVideo.setDataAndType(Uri.parse(videoLink), "video/mp4");
//                    playVideo.putExtra("title", animeName.replace("-", " ")+" Ep "+targetEp);
//                    startActivity(playVideo);
//                }
            }

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
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(AnimeDetailsActivity.this).setNamespace(DownloadsActivity.FETCH_NAMESPACE).build();
        Fetch fetch = Fetch.Impl.getInstance(fetchConfiguration);

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