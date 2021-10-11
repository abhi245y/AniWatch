package com.abhi245y.aniwatch.ui;

import static com.google.android.exoplayer2.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.Adaptors.VideoListRvAdaptor;
import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.backend.MongoDBAuth;
import com.abhi245y.aniwatch.backend.VideoCache;
import com.abhi245y.aniwatch.backend.ViewScreenshot;
import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.AnimeMongo;
import com.abhi245y.aniwatch.datamodels.EpisodesListRVDataModel;
import com.abhi245y.aniwatch.datamodels.PlayerActivityDataModel;
import com.abhi245y.aniwatch.services.AniWatchApiService;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.options.FindOneAndModifyOptions;
import io.realm.mongodb.mongo.result.InsertOneResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class VideoPlayerActivity extends AppCompatActivity {

    ProgressBar bufferProgress;
    PlayerView playerView;
    SimpleExoPlayer simpleExoPlayer;
    ToggleButton playPause;
    BottomSheetBehavior<View> mBottomSheetBehavior;
    DefaultTimeBar exoProgress;
    View bottomSheet, paddingTop;
    LinearLayout exoTiming;
    ConstraintLayout controlsContainer, episodeLayout;
    SeekBar brightSeek, volumeSeekbar;
    AudioManager audioManager;
    TextView titleName;
    RecyclerView videoListRv;
    MaterialButton exoNextThumbBtn, skipIntro, episodesList, moreInfo, forward_10, rewind_10, playbackSpeed, exoNext;
    CoordinatorLayout playerControlsView;
    ImageView nextEpThumbnail;

    //More Info views
    ImageView animePoster;
    TextView animeNameTv, seasonTypeTv, genreTv, releaseTv, statusTv, plotTv;
    MaterialButton closeMoreInfo;
    ConstraintLayout moreInfoLay;

    ViewGroup.LayoutParams ogParams;

    ExecutorService executor;

    VideoListRvAdaptor videoListRvAdaptor;
    PlayerActivityDataModel playerActivityDataModel;
    String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
    Retrofit aniRetrofit;
    AniWatchApiService aniWatchApiService;
    ArrayList<EpisodesListRVDataModel> episodesListRVDataModelArrayList;
    RetrieveVideoThumpTask task;
    Call<AniApiDownloadModel> aniApiDownloadModelCall;
    String currentEpisode, originalEpNum, currentVidUri;
    boolean onPauseCheck, onPlayerLaunchFirst;
    long playbackPosition, playPos, totalDuration;
    int initialRingMode, playlistSize, reverseOrder;
    private Handler handler;
    long timeUtils = 0;


    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        onPlayerLaunchFirst = true;
        playerView = findViewById(R.id.exoPlayerView);
        playPause = findViewById(R.id.exo_play_pause);
        exoProgress = findViewById(R.id.exo_progress);
        exoTiming = findViewById(R.id.exo_timing);
        playPause.setChecked(false);
        playPause.setVisibility(View.GONE);
        bufferProgress = findViewById(R.id.buffering_progress);
        bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        paddingTop = findViewById(R.id.paddingTop);
        videoListRv = findViewById(R.id.video_list_rv);
        controlsContainer = findViewById(R.id.controls_container);
        brightSeek = findViewById(R.id.bright_seek);
        volumeSeekbar = findViewById(R.id.vol_seek);
        titleName = findViewById(R.id.title_name);
        skipIntro = findViewById(R.id.skip_intro_btn);
        moreInfo = findViewById(R.id.anime_more_info_btn);
        episodesList = findViewById(R.id.ep_list_btn);
        forward_10 = findViewById(R.id.forward_10_sec_btn);
        rewind_10 = findViewById(R.id.rewind_10_sec_btn);
        playbackSpeed = findViewById(R.id.exo_playback_speed);
        exoNextThumbBtn = findViewById(R.id.exo_next_thumb);
        playerControlsView = findViewById(R.id.playerControls);
        nextEpThumbnail = findViewById(R.id.next_ep_thumbnail);
        exoNext = findViewById(R.id.exo_next);
        episodeLayout = findViewById(R.id.episodes_layout);


        //More Info views
        animePoster =findViewById(R.id.anime_poster_tv);
        animeNameTv = findViewById(R.id.animeName_tv);
        seasonTypeTv =findViewById(R.id.season_tv);
        genreTv =findViewById(R.id.genre_tv);
        releaseTv =findViewById(R.id.release_tv);
        statusTv =findViewById(R.id.status_tv);
        plotTv =findViewById(R.id.plot_tv);
        closeMoreInfo = findViewById(R.id.close_more_info_btn);
        moreInfoLay = findViewById(R.id.more_info_lay);

        playerActivityDataModel = getIntent().getParcelableExtra("playerData");
        currentEpisode = playerActivityDataModel.getCurrentEpNum();
        originalEpNum = playerActivityDataModel.getCurrentEpNum();
        playPos = playerActivityDataModel.getLastPlaybackPos();
        titleName.setText(playerActivityDataModel.getTitleName().replace("-", " ")+" Episode "
                +currentEpisode);

        setupEpisodesList(playerActivityDataModel.getTotalEp());
        ogParams = exoNext.getLayoutParams();


        currentVidUri = playerActivityDataModel.getCurrentEpLink();

        aniRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_ANI_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        aniWatchApiService = aniRetrofit.create(AniWatchApiService.class);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initialRingMode = audioManager.getRingerMode();
        changeRingMode();

        handler = new Handler();

        DataSource.Factory cacheDataSourceFactory =
                new CacheDataSource.Factory()
                        .setCache(VideoCache.getInstance(this));
        simpleExoPlayer = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory)).build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();

        simpleExoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC);

        simpleExoPlayer.setAudioAttributes(audioAttributes,true);

        setupMoreInfo(playerActivityDataModel.getAnimeId());
        setVolumeAndBrightness();
        setupPlayerControls();
        setupPlayer(currentVidUri);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupEpisodesList(String totalEps) {
        episodesListRVDataModelArrayList = new ArrayList<>();
        for (int i = 0; i < Float.parseFloat(totalEps); i++) {
            String epsCount = String.valueOf(1 + i);
            executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                //Background work here
                retrieveVideoFrameFromVideo(playerActivityDataModel.getCurrentEpLink(), epsCount);
                handler.post(() -> {
                    //UI Thread work here
                    videoListRvAdaptor.notifyDataSetChanged();
                });
            });
        }

        videoListRvAdaptor = new VideoListRvAdaptor(episodesListRVDataModelArrayList, VideoPlayerActivity.this);
        videoListRv.setAdapter(videoListRvAdaptor);
        videoListRv.setLayoutManager(new LinearLayoutManager(VideoPlayerActivity.this, LinearLayoutManager.HORIZONTAL, false));
        episodesList.setClickable(true);
        MaterialButton episodesListClose = findViewById(R.id.close_episode_list_btn);
        episodesList.setOnClickListener(view -> {
            moreInfoLay.setVisibility(View.GONE);
            episodeLayout.setVisibility(View.VISIBLE);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        episodesListClose.setOnClickListener(view -> {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            simpleExoPlayer.play();
            playPause.setChecked(true);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void retrieveVideoFrameFromVideo(String videoPath, String epsCount) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
//            synchronized (VideoPlayerActivity.this){
//
//            }
            bitmap = mediaMetadataRetriever.getFrameAtTime(200000000+timeUtils, MediaMetadataRetriever.OPTION_CLOSEST);

            EpisodesListRVDataModel episodesListRVDataModel =
                    new EpisodesListRVDataModel("", epsCount,bitmap, simpleExoPlayer);
            episodesListRVDataModelArrayList.add(episodesListRVDataModel);
            Log.d("TAG","episodesListRVDataModel "+ episodesListRVDataModel);
            timeUtils = timeUtils+180000000;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (mediaMetadataRetriever != null)
            {
                mediaMetadataRetriever.release();
            }
        }




    }

    private void setupMoreInfo(String animeId) {

        App app = new MongoDBAuth().getMongoApp();
        String savedUsername= new MongoDBAuth().getUsername(this);
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), result -> {
            if(result.isSuccess()) {
                Log.v("RecentReleasedFragment", "successfully Logged in");
                User user = app.currentUser();
                assert user != null;
                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");
                CodecRegistry pojoCodecRegistry = fromRegistries(AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY,
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

                MongoCollection<AnimeMongo> animeMongoMongoCollection =
                        mongoDatabase.getCollection("anime", AnimeMongo.class).withCodecRegistry(pojoCodecRegistry);

                Document animeQuery = new Document("gogoVcID", animeId);

                animeMongoMongoCollection.find(animeQuery).first().getAsync(result1 -> {
                    AnimeMongo animeMongo = result1.get();

                    Glide.with(getApplicationContext()).load(animeMongo.getImgLink()).centerCrop().into(animePoster);

                    animeNameTv.setText(animeMongo.getTitleName());
                    seasonTypeTv.setText(animeMongo.getSeason());
                    String genres =  animeMongo.getGenre().toString().replace("[","").replace("]","");
                    genreTv.setText(genres);
                    releaseTv.setText(animeMongo.getReleased());
                    statusTv.setText(animeMongo.getStatus());
                    plotTv.setText(animeMongo.getSummary());


                });


            }
        });

        moreInfo.setOnClickListener(view ->{
            episodeLayout.setVisibility(View.GONE);
            moreInfoLay.setVisibility(View.VISIBLE);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        closeMoreInfo.setOnClickListener(view -> {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            simpleExoPlayer.play();
            playPause.setChecked(true);
        });


    }


    private void getDownloadLink(String epNum) {
        aniApiDownloadModelCall = aniWatchApiService.getVideoLink(playerActivityDataModel.getAnimeId(), epNum);

        aniApiDownloadModelCall.enqueue(new Callback<AniApiDownloadModel>() {
            @Override
            public void onResponse(@NonNull Call<AniApiDownloadModel> call, @NonNull Response<AniApiDownloadModel> response) {
                AniApiDownloadModel res = response.body();
                try {
                    if (res != null) {
                        if (res.getDl_link().contains("https://streamsb.com")){
                            getDownloadLink(epNum);
                        }else {
                            task = new RetrieveVideoThumpTask();
                            task.execute(res.getDl_link());

                            MediaMetadata mediaMetadata = new MediaMetadata.Builder().setMediaUri(Uri.parse(res.getDl_link())).setTrackNumber(Integer.valueOf(epNum))
                                    .build();
                            MediaItem nextEp = new MediaItem.Builder().setUri(res.getDl_link()).setMediaMetadata(mediaMetadata).build();
                            Log.d("Uri","Recycler: mediaId "+mediaMetadata.mediaUri);

                            simpleExoPlayer.addMediaSource(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(VideoPlayerActivity.this)).createMediaSource(nextEp));

                        }

                    }else{
                        getDownloadLink(epNum);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AniApiDownloadModel> call, @NonNull Throwable t) {

            }
        });


    }


    private void setupPlayerControls() {

        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    paddingTop.setVisibility(View.GONE);
                    titleName.setVisibility(View.VISIBLE);
                    controlsContainer.setVisibility(View.VISIBLE);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (nextEpThumbnail.getVisibility()==View.VISIBLE){
                        nextEpThumbnail.setVisibility(View.INVISIBLE);
                    }
                    titleName.setVisibility(View.INVISIBLE);
                    controlsContainer.setVisibility(View.INVISIBLE);
                    simpleExoPlayer.pause();
                    playPause.setChecked(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


//        playerControlsView.setOnTouchListener(new OnSwipeTouchListener(VideoPlayerActivity.this) {
//            public void onSwipeTop() {
////                if(episodesListRVDataModelArrayList!=null && episodesListRVDataModelArrayList.size()!=0) {
////                    paddingTop.setVisibility(View.VISIBLE);
////                    controlsContainer.setVisibility(View.GONE);
////                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
////                }
//            }
//            public void onSwipeBottom() {
////                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//
//            }
//            public void onSwipeRight(){
//                simpleExoPlayer.seekForward();
//                playbackPosition = simpleExoPlayer.getCurrentPosition();
//            }
//            public void onSwipeLeft(){
//                simpleExoPlayer.seekBack();
//                playbackPosition = simpleExoPlayer.getCurrentPosition();
//            }
//
//        });
    }

    private void setupPlayer(String mediaUri) {
        Log.d("Life", "setup currentVidUri "+ mediaUri);
        Log.d("Life", "setup position "+ playbackPosition);
        playerView.setPlayer(simpleExoPlayer);
        onPauseCheck = true;

        MediaMetadata mediaMetadata = new MediaMetadata.Builder().setMediaUri(Uri.parse(mediaUri)).setDisplayTitle(titleName.getText()).setTrackNumber(Integer.valueOf(currentEpisode))
                .build();
        MediaItem mediaItem = new MediaItem.Builder().setUri(mediaUri).setMediaMetadata(mediaMetadata).build();

        simpleExoPlayer.setMediaSource(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(VideoPlayerActivity.this)).createMediaSource(mediaItem));
        simpleExoPlayer.prepare();
        if (playPos!=0) {
            simpleExoPlayer.seekTo(playPos);
        }

        skipIntro.setOnClickListener(view -> {
            long skipIntroVal = (long) (simpleExoPlayer.getDuration()*(11.0f/100.0f));
            simpleExoPlayer.seekTo(skipIntroVal);
            Log.d("TAG", "skipIntroVal "+ skipIntroVal);

        });
        forward_10.setOnClickListener(view -> simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition()+10000));
        rewind_10.setOnClickListener(view -> simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition()-10000));
        playbackSpeed.setOnClickListener(view -> {
            if (playbackSpeed.getText().toString().contains("1x")) {
                float speed = 2f;
                PlaybackParameters param = new PlaybackParameters(speed);
                simpleExoPlayer.setPlaybackParameters(param);
                playbackSpeed.setText("Speed (2x)");
            }else if (playbackSpeed.getText().toString().contains("2x")){
                float speed = 3f;
                PlaybackParameters param = new PlaybackParameters(speed);
                simpleExoPlayer.setPlaybackParameters(param);
                playbackSpeed.setText("Speed (3x)");
            }else if (playbackSpeed.getText().toString().contains("3x")){
                float speed = 1f;
                PlaybackParameters param = new PlaybackParameters(speed);
                simpleExoPlayer.setPlaybackParameters(param);
                playbackSpeed.setText("Speed (1x)");
            }

        });
        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if(playbackState == Player.STATE_BUFFERING){
                    playPause.setVisibility(View.INVISIBLE);
                    bufferProgress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    playPause.setVisibility(View.VISIBLE);
                    bufferProgress.setVisibility(View.GONE);
                    totalDuration = simpleExoPlayer.getDuration();

                    if (onPlayerLaunchFirst){
                        Log.d("TAG", "onPlayerLaunchFirst "+ onPlayerLaunchFirst);
                        saveHistory(playPos, totalDuration);
                        onPlayerLaunchFirst = false;
                    }

                }else playerView.setKeepScreenOn(playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED);

                if (playbackState == Player.STATE_ENDED){
//                    retrieveNextEpisode();
                    episodesListRVDataModelArrayList.clear();
                }

            }
        });

        playerView.setKeepScreenOn(true);
        simpleExoPlayer.setWakeMode(1);

        Log.d("TAG", "playlistSize "+ playlistSize);
        if (playlistSize == 0){
            int totalEps = Integer.parseInt(playerActivityDataModel.getTotalEp());
            if (totalEps>=2){
                if(totalEps!=Integer.parseInt(currentEpisode)){
                    int nextEp = Integer.parseInt(currentEpisode)+1;
                    getDownloadLink(String.valueOf(nextEp));
                }
//                else if(episodesListRVDataModelArrayList!=null){
//                    int nextEp = Integer.parseInt(currentEpisode)-1;
//                    getDownloadLink(String.valueOf(nextEp));
//                }
            }
        }

        playPause.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                simpleExoPlayer.play();
                changeRingMode();
                playerView.setKeepScreenOn(true);
            } else {
                simpleExoPlayer.pause();
                audioManager.setRingerMode(initialRingMode);
//                playerView.setKeepScreenOn(false);
            }

        });

        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem,
                                              @Player.Event int reason) {
                Log.v("VideoPlayerActivity","onMediaItemTransition: "+reason);
                if(reason == MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                    playlistSize = 2;
                    nextEpThumbnail.setVisibility(View.GONE);
                    exoNextThumbBtn.setVisibility(View.GONE);
                    if (mediaItem != null) {
                        currentEpisode = String.valueOf(mediaItem.mediaMetadata.trackNumber);
                        currentVidUri = String.valueOf(mediaItem.mediaMetadata.mediaUri);
                        titleName.setText(playerActivityDataModel.getTitleName().replace("-", " ")+ " Episode " + currentEpisode);
                    } else {
                        titleName.setText(playerActivityDataModel.getTitleName().replace("-", " ")
                                + " Episode " + originalEpNum);
                    }
                    int totalEps = Integer.parseInt(playerActivityDataModel.getTotalEp());
                    if (totalEps >= 2) {
                        if (totalEps != Integer.parseInt(currentEpisode) && reverseOrder == 0) {
                            int nextEp = Integer.parseInt(currentEpisode) + 1;
                            getDownloadLink(String.valueOf(nextEp));
                        }
//                        else if (episodesListRVDataModelArrayList != null) {
//                            reverseOrder = 1;
//                            int nextEp = Integer.parseInt(currentEpisode) - 1;
//                            getDownloadLink(String.valueOf(nextEp));
//                        }
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (!isPlaying) {
                    playbackPosition = simpleExoPlayer.getCurrentPosition();
                    totalDuration = simpleExoPlayer.getDuration();
                    saveHistory(playbackPosition, totalDuration);
                }
            }

            @Override
            public void onVolumeChanged(float volume) {
                volumeSeekbar.setProgress((int) volume);
            }



            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e("VideoPlayerActivity","Player Error "+error);
                addLinkToErrorList(error.getErrorCodeName());

            }
        });

        ConstraintLayout.LayoutParams nextThumbBtnParams = (ConstraintLayout.LayoutParams) exoNextThumbBtn.getLayoutParams();


        Runnable runnable = new Runnable() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void run() {
                try {
                    if (Integer.parseInt(playerActivityDataModel.getTotalEp()) != Integer.parseInt(currentEpisode)) {
                        float remaining = simpleExoPlayer.getCurrentPosition() * 100.0f / simpleExoPlayer.getDuration();
                        if (remaining >= 92) {
                            if (mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
                                nextEpThumbnail.setVisibility(View.INVISIBLE);
                            }else if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED)  {
                                nextEpThumbnail.setVisibility(View.VISIBLE);
                            }
                            exoNext.setLayoutParams(nextThumbBtnParams);
                            exoNext.setText("Next Ep");
                        } else {
                            exoNext.setLayoutParams(ogParams);
                            nextEpThumbnail.setVisibility(View.GONE);
                        }
                        handler.postDelayed(this, 1000);
                    }
                } catch (IllegalStateException ed){
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 10);

    }

    private void addLinkToErrorList(String playbackException) {
        App app = new MongoDBAuth().getMongoApp();
        String savedUsername= new MongoDBAuth().getUsername(this);
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {

                User user = app.currentUser();

                assert user != null;
                String currentEpTitle = titleName.getText().toString();
                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");

                Document errorDetails = new Document("id",savedUsername).append("currentEpTitle", currentEpTitle)
                        .append("gogoVcID", playerActivityDataModel.getAnimeId()).append("playbackException", playbackException)
                        .append("videoLink", currentVidUri);

                mongoDatabase.getCollection("video_playback_errors").insertOne(errorDetails).getAsync(new App.Callback<InsertOneResult>() {
                    @Override
                    public void onResult(App.Result<InsertOneResult> result) {
                        if (result.isSuccess()){
                            Log.v("VideoPlayerActivity", "Error Reported");
                        }else{
                            Log.v("VideoPlayerActivity", "Saving error: "+result.getError());
                        }
                    }
                });
            }
        });
    }

    public void changeRingMode(){
        try {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }catch (Exception ignored){

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setVolumeAndBrightness() {
        try {
            boolean canWrite = Settings.System.canWrite(getApplicationContext());
            if(!canWrite){
                Intent manageSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(manageSettings);
            }
            int cBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            brightSeek.setProgress(cBrightness);

            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });

            brightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    int sBrightness = i*255/255;
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, sBrightness);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(aniApiDownloadModelCall!=null){
            aniApiDownloadModelCall.cancel();
        }
        if(task!=null){
            task.cancel(true);
        }
        simpleExoPlayer.pause();
        playbackPosition = simpleExoPlayer.getCurrentPosition();
        if(playbackPosition!=0){
            playPos = playbackPosition;
            saveHistory(playPos, totalDuration);
        }
        audioManager.setRingerMode(initialRingMode);
    }

    @Override
    public void onStart() {
        super.onStart();
        playerControlsView.setVisibility(View.VISIBLE);
        controlsContainer.setVisibility(View.VISIBLE);
        titleName.setVisibility(View.VISIBLE);
        playPause.setChecked(false);
        changeRingMode();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(playPos!=0){
            saveHistory(playPos, totalDuration);
        }else {
            saveHistory(playbackPosition, totalDuration);
        }

        if (playerView != null) {
            playerView.onPause();
        }
        if(aniApiDownloadModelCall!=null){
            aniApiDownloadModelCall.cancel();
        }
        if(task!=null){
            task.cancel(true);
        }
        if (executor!=null){
            executor.shutdownNow();
        }

        audioManager.setRingerMode(initialRingMode);
        simpleExoPlayer.stop();
        simpleExoPlayer.release();
    }

    @SuppressLint("StaticFieldLeak")
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @Data
    private class RetrieveVideoThumpTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = null;
            MediaMetadataRetriever mediaMetadataRetriever = null;
            try
            {
                mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(url, new HashMap<>());
                synchronized (this){
                    bitmap = mediaMetadataRetriever.getFrameAtTime(100000000, MediaMetadataRetriever.OPTION_CLOSEST);

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (mediaMetadataRetriever != null)
                {
                    mediaMetadataRetriever.release();
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            nextEpThumbnail.setImageBitmap(result);

        }
    }


    public void saveHistory(long lastPlaybackPos, long totalVidDuration){

        playerControlsView.setVisibility(View.GONE);

        App app = new MongoDBAuth().getMongoApp();
        String savedUsername= new MongoDBAuth().getUsername(this);
        app.loginAsync(Credentials.customFunction(new org.bson.Document("username", savedUsername)), result -> {
            if(result.isSuccess()) {
                User user = app.currentUser();
                assert user != null;
                String currentEpTitle = titleName.getText().toString();
                String currentEpNum = currentEpisode;
                String currentDate$Time = String.valueOf(Calendar.getInstance().getTime());
                MongoDatabase mongoDatabase = user.getMongoClient("mongodb-atlas").getDatabase("main");

                Document history = new Document("gogoVcID", playerActivityDataModel.getAnimeId()).append("titleName", playerActivityDataModel.getTitleName())
                        .append("totalEpisodes", playerActivityDataModel.getTotalEp())
                        .append("currentEpTitle", currentEpTitle).append("currentEpNum", currentEpNum)
                        .append("lastPlaybackPos", lastPlaybackPos).append("totalVidDuration", totalVidDuration).append("thumpImageUri", "").append("lastupated", currentDate$Time);


                Log.v("VideoPlayerActivity", "currentEpTitle: "+currentEpTitle);
                mongoDatabase.getCollection("users").findOne(new Document("username",  savedUsername).append("history_list.currentEpTitle", currentEpTitle)).getAsync(result1 -> {
                   try {
                       if (result1.get()!=null) {
                           Log.v("VideoPlayerActivity", " Already In History: ");
                           mongoDatabase.getCollection("users")
                                   .findOneAndUpdate(new Document("username",  savedUsername).append("history_list.currentEpTitle", currentEpTitle),
                                           new Document("$set", new Document("history_list.$.lastPlaybackPos", lastPlaybackPos)
                                                   .append("history_list.$.totalVidDuration", totalVidDuration).append("history_list.$.lastupated", currentDate$Time))).getAsync(result2 -> {
                                               if (result2.isSuccess()) {
                                                   if (result2.get() != null) {
                                                       Log.v("VideoPlayerActivity", "successfully data updated  history: " + result2.get());
                                                   }
                                               } else {
                                                   Log.e("VideoPlayerActivity", "Error data updating history " + result2.getError());
                                               }
                                           });
                       }else {
                           Log.v("VideoPlayerActivity", "Adding To history");
                           mongoDatabase.getCollection("users")
                                   .findOneAndUpdate(new Document("username",  savedUsername), new Document("$push", new Document("history_list", history)),
                                           new FindOneAndModifyOptions().returnNewDocument(true)).getAsync(result3 -> {
                               if (result3.isSuccess()){

                                   Log.v("VideoPlayerActivity", "successfully data added to history: "+result3.get());

                               }else {
                                   Log.e("VideoPlayerActivity", "Error data adding to history "+ result3.getError());
                               }
                           });
                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                });
                addThumpToBackBlaze(currentEpTitle);


            }
        });
    }

    private void addThumpToBackBlaze(String fileName) {

        ViewScreenshot viewScreenshot = new ViewScreenshot();
        viewScreenshot.take(playerControlsView,playerView.getVideoSurfaceView(), VideoPlayerActivity.this, new ViewScreenshot.PostTake() {
            @Override
            public void onSuccess(Bitmap bitmap) {

                Log.d("VideoPlayerActivity", "bitmap: " + bitmap);
                try {
                    playerControlsView.setVisibility(View.VISIBLE);
                    String dirpath = "/storage/emulated/0/Android/data/" + getApplicationContext().getPackageName();
                    File file = new File(dirpath);
                    if (!file.exists()) {
                        boolean mkdir = file.mkdir();
                    }

                    String path = dirpath + "/cache/" + fileName + ".jpeg";
                    File imageurl = new File(path);
                    FileOutputStream outputStream = new FileOutputStream(imageurl);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    uploadFile(imageurl, fileName);




                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int error) {

            }

        });
    }

    private void uploadFile(File originalFile, String filename) {


        RequestBody descriptionPart = RequestBody.create(filename, MultipartBody.FORM);


        RequestBody filePart = RequestBody.create(originalFile, MediaType.parse("image/*"));

        MultipartBody.Part file = MultipartBody.Part.createFormData("file", originalFile.getName(), filePart);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://aniwatch-api.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        AniWatchApiService client = retrofit.create(AniWatchApiService.class);
        String savedUsername= new MongoDBAuth().getUsername(this);
        Call<ResponseBody> call = client.uploadPhoto(descriptionPart, file, savedUsername);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        originalFile.deleteOnExit();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

            }
        });

    }
}