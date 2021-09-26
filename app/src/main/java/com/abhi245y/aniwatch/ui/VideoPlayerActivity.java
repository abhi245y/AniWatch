package com.abhi245y.aniwatch.ui;

import static com.google.android.exoplayer2.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK;
import static com.google.android.exoplayer2.Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.abhi245y.aniwatch.backend.OnSwipeTouchListener;
import com.abhi245y.aniwatch.backend.VideoCache;
import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.EpisodesListRVDataModel;
import com.abhi245y.aniwatch.datamodels.PlayerActivityDataModel;
import com.abhi245y.aniwatch.services.AniWatchApiService;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
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
    BottomSheetBehavior mBottomSheetBehavior;
    DefaultTimeBar exoProgress;
    View bottomSheet, paddingTop;
    LinearLayout exoTiming;
    ConstraintLayout controlsContainer;
    SeekBar brightSeek, volumeSeekbar;
    AudioManager audioManager;
    TextView titleName;
    RecyclerView videoListRv;

    VideoListRvAdaptor videoListRvAdaptor;
    PlayerActivityDataModel playerActivityDataModel;
    String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
    Retrofit aniRetrofit;
    AniWatchApiService aniWatchApiService;
    ArrayList<EpisodesListRVDataModel> episodesListRVDataModelArrayList;
    RetrieveVideoThumpTask task;
    Call<AniApiDownloadModel> aniApiDownloadModelCall;
    String currentEpisode, originalEpNum, currentVidUri;
    boolean nextEpState, onPauseCheck;
    long playbackPosition;
    long playPos;
    int initialRingMode, playlistSize;



    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

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
        playerActivityDataModel = getIntent().getParcelableExtra("playerData");
        currentEpisode = playerActivityDataModel.getCurrentEpNum();
        originalEpNum = playerActivityDataModel.getCurrentEpNum();
        titleName.setText(playerActivityDataModel.getAnimeId().replace("-", " ").toUpperCase()+" EPISODE "
                +currentEpisode);

        currentVidUri = playerActivityDataModel.getCurrentEpLink();

        aniRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_ANI_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        aniWatchApiService = aniRetrofit.create(AniWatchApiService.class);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initialRingMode = audioManager.getRingerMode();
        changeRingMode();


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

        setVolumeAndBrightness();
        setupPlayerControls();
        episodesListRVDataModelArrayList = new ArrayList<>();
        videoListRvAdaptor = new VideoListRvAdaptor(episodesListRVDataModelArrayList, VideoPlayerActivity.this);
        videoListRv.setAdapter(videoListRvAdaptor);
        videoListRv.setLayoutManager(new LinearLayoutManager(VideoPlayerActivity.this, LinearLayoutManager.HORIZONTAL, false));
        setupPlayer(currentVidUri);

    }

    private void retrieveNextEpisode() {
        int totalEps = Integer.parseInt(playerActivityDataModel.getTotalEp());
        if (totalEps>=2){
            if(totalEps!=Integer.parseInt(currentEpisode)){
                int nextEp = Integer.parseInt(currentEpisode)+1;
                nextEpState = true;
                if(totalEps<5) {
                    for (int i = 1; i <= totalEps; i++) {
                        makePlayList(String.valueOf(nextEp));
                        nextEp++;
                    }
                }else {
                    for (int i = 1; i <=5; i++) {
                        makePlayList(String.valueOf(nextEp));
                        nextEp++;
                    }
                }
            }else {
                int nextEp = Integer.parseInt(currentEpisode)-1;
                nextEpState = false;
                if(totalEps<5) {
                    for (int i = 1; i <= totalEps; i++) {
                        makePlayList(String.valueOf(nextEp));
                        nextEp--;
                    }
                }else {
                    for (int i = 1; i <=5; i++) {
                        makePlayList(String.valueOf(nextEp));
                        nextEp--;
                    }
                }
            }
        }
    }


    private void makePlayList(String epNum) {

        if (playerActivityDataModel.getFromActivity().equals("recent_release")) {

            getDownloadLink(epNum, playerActivityDataModel.getFromActivity(),
                    playerActivityDataModel.getEpGogoLink().replace("https://gogoanime.app/anime/", ""));

        }else {
            getDownloadLink(epNum, playerActivityDataModel.getFromActivity(), "n/a");
        }

    }

    private void getDownloadLink(String epNum, String fromActivity, String epLink) {
        aniApiDownloadModelCall = aniWatchApiService.videoLink(playerActivityDataModel.getAnimeId(),
                epNum, fromActivity, epLink);

        aniApiDownloadModelCall.enqueue(new Callback<AniApiDownloadModel>() {
            @Override
            public void onResponse(@NonNull Call<AniApiDownloadModel> call, @NonNull Response<AniApiDownloadModel> response) {
                AniApiDownloadModel res = response.body();
                try {
                    if (res != null) {
                        task = new RetrieveVideoThumpTask(res, epNum, simpleExoPlayer);
                        task.execute(res.getDl_link());

                        MediaMetadata mediaMetadata = new MediaMetadata.Builder().setMediaUri(Uri.parse(res.getDl_link())).setTrackNumber(Integer.valueOf(epNum))
                                .build();
                        MediaItem nextEp = new MediaItem.Builder().setUri(res.getDl_link()).setMediaMetadata(mediaMetadata).build();
                        Log.d("Uri","Recycler: mediaId "+mediaMetadata.mediaUri);

                        simpleExoPlayer.addMediaSource(new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(VideoPlayerActivity.this)).createMediaSource(nextEp));

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
                    paddingTop.setVisibility(View.GONE);
                    controlsContainer.setVisibility(View.VISIBLE);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    simpleExoPlayer.pause();
                    playPause.setChecked(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        CoordinatorLayout coordinatorLayout = findViewById(R.id.playerControls);
        coordinatorLayout.setOnTouchListener(new OnSwipeTouchListener(VideoPlayerActivity.this) {
            public void onSwipeTop() {
                if(episodesListRVDataModelArrayList!=null && episodesListRVDataModelArrayList.size()!=0) {
                    paddingTop.setVisibility(View.VISIBLE);
                    controlsContainer.setVisibility(View.GONE);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
            public void onSwipeBottom() {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
            public void onSwipeRight(){
                simpleExoPlayer.seekForward();
                playbackPosition = simpleExoPlayer.getCurrentPosition();
            }
            public void onSwipeLeft(){
                simpleExoPlayer.seekBack();
                playbackPosition = simpleExoPlayer.getCurrentPosition();
            }

        });
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

        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if(playbackState == Player.STATE_BUFFERING){
                    playPause.setVisibility(View.INVISIBLE);
                    bufferProgress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    playPause.setVisibility(View.VISIBLE);
                    bufferProgress.setVisibility(View.GONE);

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
                    makePlayList(String.valueOf(nextEp));
                }else if(episodesListRVDataModelArrayList!=null){
                    int nextEp = Integer.parseInt(currentEpisode)-1;
                    makePlayList(String.valueOf(nextEp));
                }
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
                playerView.setKeepScreenOn(false);
            }

        });

        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem,
                                              @Player.MediaItemTransitionReason int reason) {
                if(reason == MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                    playlistSize = 2;
                    if (mediaItem != null) {
                        currentEpisode = String.valueOf(mediaItem.mediaMetadata.trackNumber);
                        currentVidUri = String.valueOf(mediaItem.mediaMetadata.mediaUri);
                        titleName.setText(playerActivityDataModel.getAnimeId().replace("-", " ").toUpperCase() + " EPISODE " + currentEpisode);
                    } else {
                        titleName.setText(playerActivityDataModel.getAnimeId().replace("-", " ").toUpperCase()
                                + " EPISODE " + originalEpNum);
                    }
                    int totalEps = Integer.parseInt(playerActivityDataModel.getTotalEp());
                    if (totalEps >= 2) {
                        if (totalEps != Integer.parseInt(currentEpisode)) {
                            int nextEp = Integer.parseInt(currentEpisode) + 1;
                            makePlayList(String.valueOf(nextEp));
                        } else if (episodesListRVDataModelArrayList != null) {
                            int nextEp = Integer.parseInt(currentEpisode) - 1;
                            makePlayList(String.valueOf(nextEp));
                        }
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (!isPlaying) {
                    playbackPosition = simpleExoPlayer.getCurrentPosition();
                }
            }

            @Override
            public void onTimelineChanged(@NonNull Timeline timeline, @Player.TimelineChangeReason int reason) {
                if (reason == TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
//                    isPlaylistEmpty = true;
                }
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

    public Bitmap retrieveVideoFrameFromVideo(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
            synchronized (VideoPlayerActivity.this){
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
        }
        audioManager.setRingerMode(initialRingMode);
    }

    @Override
    public void onStart() {
        super.onStart();
        changeRingMode();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (playerView != null) {
            playerView.onPause();
        }
        if(aniApiDownloadModelCall!=null){
            aniApiDownloadModelCall.cancel();
        }
        if(task!=null){
            task.cancel(true);
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
        private final AniApiDownloadModel aniApiDownloadModelWeakReference;
        private final String epNum;
        private final SimpleExoPlayer simpleExoPlayer;


        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            return retrieveVideoFrameFromVideo(url);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            EpisodesListRVDataModel episodesListRVDataModel =
                    new EpisodesListRVDataModel(aniApiDownloadModelWeakReference.getDl_link(), epNum,result, simpleExoPlayer);
            episodesListRVDataModelArrayList.add(episodesListRVDataModel);
            
        }
    }


}