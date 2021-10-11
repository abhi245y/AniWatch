package com.abhi245y.aniwatch.Adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.datamodels.AniApiDownloadModel;
import com.abhi245y.aniwatch.datamodels.PlayerActivityDataModel;
import com.abhi245y.aniwatch.datamodels.UserHistoryMongoModel;
import com.abhi245y.aniwatch.services.AniWatchApiService;
import com.abhi245y.aniwatch.ui.VideoPlayerActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HistoryListAdaptor extends RecyclerView.Adapter<HistoryListAdaptor.ViewHolder>{

    ArrayList<UserHistoryMongoModel> historyMongoModelArrayList;
    Context context;

    public HistoryListAdaptor(ArrayList<UserHistoryMongoModel> historyMongoModelArrayList, Context context) {
        this.historyMongoModelArrayList = historyMongoModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.watch_history_recy_model, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @SneakyThrows
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserHistoryMongoModel userHistoryMongoModel = historyMongoModelArrayList.get(position);

        try{
            Glide.with(context).load(userHistoryMongoModel.getThumpImageUri()).centerCrop().into(holder.videoThumb);

            holder.titleName.setText(userHistoryMongoModel.getTitleName());
            String currentEP = "Episode "+userHistoryMongoModel.getCurrentEpNum();
            holder.epNum.setText(currentEP);

            holder.videoDuration.setText(stringForTime(userHistoryMongoModel.getTotalVidDuration()));
            holder.videoWatchedProgress.setMax((int) userHistoryMongoModel.getTotalVidDuration());
            holder.videoWatchedProgress.setProgress((int) userHistoryMongoModel.getLastPlaybackPos(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String stringForTime(long timeMs) {
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        long totalSeconds =  (timeMs / 1000);
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public int getItemCount() {
        return historyMongoModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView videoThumb, gifImageView;
        ProgressBar videoWatchedProgress;
        TextView titleName, epNum, videoDuration, dialogue, tv3;
        CountDownTimer countDownTimer;
        View customLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            videoThumb = itemView.findViewById(R.id.thumbnail);
            videoWatchedProgress = itemView.findViewById(R.id.watch_history_progress);
            titleName = itemView.findViewById(R.id.titleName_tv);
            epNum = itemView.findViewById(R.id.ep_num_tv);
            videoDuration = itemView.findViewById(R.id.video_duration_tv);
            tv3 =itemView.findViewById(R.id.tv_3);

            customLayout = LayoutInflater.from(context).inflate(R.layout.custom_loading_alert_layout, null);
            gifImageView = customLayout.findViewById(R.id.loading_gif_img);
            dialogue = customLayout.findViewById(R.id.video_link_alert_dialogue);

            itemView.setOnClickListener(view -> {
                AlertDialog dialog = null;
                try{
                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(context);
                    builder.setView(customLayout);
                    dialog = builder.create();
                    dialog.show();

                    countDownTimer = new CountDownTimer(6000,100) {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if(millisUntilFinished/1000 == 5){
                                Glide.with(context).asGif().load(R.drawable.getting_video_link).into(gifImageView);
                                dialogue.setText(R.string.getting_video_link);
                            }if(millisUntilFinished/1000 == 4){
                                Glide.with(context).asGif().load(R.drawable.hacking_mainframe).centerCrop().into(gifImageView);
                                dialogue.setText("Hacking GogoAnime");
                            }if(millisUntilFinished/1000 == 3){
                                Glide.with(context).asGif().load(R.drawable.anime_hacking).centerCrop().into(gifImageView);
                                dialogue.setText("Patching Files");
                            }
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFinish() {

                        }
                    }.start();

                    UserHistoryMongoModel result = historyMongoModelArrayList.get(getBindingAdapterPosition());
                    fetchDownloadLink(result, dialog);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (dialog != null) {
                        dialog.cancel();
                    }
                }
            });
        }

        private void fetchDownloadLink(UserHistoryMongoModel res, AlertDialog dialog) {
            String BASE_URL_ANI_API = "https://aniwatch-api.herokuapp.com/";
            Retrofit aniRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL_ANI_API)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            String anime_id = res.getGogoVcID();
            String targetEp = res.getCurrentEpNum();
            AniWatchApiService aniWatchApiService = aniRetrofit.create(AniWatchApiService.class);
            Call<AniApiDownloadModel> aniApiDownloadModelCall = aniWatchApiService.getVideoLink(anime_id, targetEp);

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
                                Log.d("TAG", "Error Getting Link id: " + anime_id);
                                countDownTimer.cancel();
                                Glide.with(context).asGif().load(R.drawable.failed).centerCrop().into(gifImageView);
                                dialogue.setText("I Failed xO");

                            }else if (result.getDl_link().contains("https://streamsb.com")){
                                countDownTimer.cancel();
                                Glide.with(context).asGif().load(R.drawable.error_trying_again).centerCrop().into(gifImageView);
                                dialogue.setText("Trying Again..");
                                countDownTimer.start();
                                fetchDownloadLink(res, dialog);
                            }else {
                                Glide.with(context).asGif().load(R.drawable.done_searching).centerCrop().into(gifImageView);
                                dialogue.setText("Done");

                                String videoLink = result.getDl_link();
                                Log.d("TAG", "videoLink: " + videoLink);

                                if (!videoLink.equals("null")) {
                                    if (!videoLink.contains("https://storage.cloud.google.com")) {
                                        Log.d("TAG", "Video URL: " + videoLink);
                                        startPlayer(anime_id,targetEp, res, videoLink, dialog);
                                    }
                                }
                            }
                        }else {
                            countDownTimer.cancel();
                            Glide.with(context).asGif().load(R.drawable.failed).centerCrop().into(gifImageView);
                            dialogue.setText("I Failed xO");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        countDownTimer.cancel();
                        Glide.with(context).asGif().load(R.drawable.failed).centerCrop().into(gifImageView);
                        dialogue.setText("I Failed xO");
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onFailure(@NonNull Call<AniApiDownloadModel> call, @NonNull Throwable t) {
                    countDownTimer.cancel();
                    Glide.with(context).asGif().load(R.drawable.failed).centerCrop().into(gifImageView);
                    dialogue.setText("I Failed xO");
                }
            });
        }

        private void startPlayer(String anime_id, String targetEp, UserHistoryMongoModel res, String download_link, AlertDialog dialog) {
            PlayerActivityDataModel playerActivityDataModel = new PlayerActivityDataModel(anime_id,
                    download_link, targetEp, res.getTotalEpisodes(), res.getTitleName(), res.getLastPlaybackPos());
            Intent playerActivity = new Intent(context, VideoPlayerActivity.class);
            playerActivity.putExtra("playerData", playerActivityDataModel);
            dialog.cancel();
            context.startActivity(playerActivity);
        }
    }
}
