package com.abhi245y.aniwatch.Adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.datamodels.AnimeMongo;
import com.abhi245y.aniwatch.datamodels.RecentReleasedAniViewModel;
import com.abhi245y.aniwatch.ui.AnimeDetailsActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecentReleasedAniViewAdaptor extends  RecyclerView.Adapter<RecentReleasedAniViewAdaptor.ViewHolder>{

    ArrayList<RecentReleasedAniViewModel> recentReleasesBeans;
    Context context;

    @SuppressLint("NotifyDataSetChanged")
    public RecentReleasedAniViewAdaptor(ArrayList<RecentReleasedAniViewModel> recentReleasesBeans, Context context) {
        this.recentReleasesBeans = recentReleasesBeans;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentReleasedAniViewAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentReleasedAniViewAdaptor.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_released_anime_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentReleasedAniViewAdaptor.ViewHolder holder, int position) {
        AnimeMongo res = recentReleasesBeans.get(position).getAnimeMongo();
        Glide.with(context).load(res.getImgLink()).centerCrop().placeholder(R.drawable.placeholder1).into(holder.aniCoverImg);
        holder.aniTitle.setText(res.getTitleName());
        holder.aniEpisode.setText(recentReleasesBeans.get(position).getCurrent_ep());

    }

    @Override
    public int getItemCount() { return recentReleasesBeans.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView aniCoverImg;
        TextView aniTitle, aniEpisode;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            aniCoverImg = itemView.findViewById(R.id.ani_cover_img);
            aniTitle = itemView.findViewById(R.id.ani_title_tv);
            aniTitle.setSelected(true);
            aniEpisode = itemView.findViewById(R.id.ani_released_ep_tv);

            itemView.setOnClickListener(view -> {
                RecentReleasedAniViewModel recentReleasesBean = recentReleasesBeans.get(getBindingAdapterPosition());
                Intent animeDetails = new Intent(context, AnimeDetailsActivity.class);
                animeDetails.putExtra("animeData", recentReleasesBean.getAnimeMongo());
                if (recentReleasesBean.getCurrent_ep().contains("Movie")){
                    animeDetails.putExtra("current_ep", "1");
                }else {
                    animeDetails.putExtra("current_ep", recentReleasesBean.getCurrent_ep().replace("Episode ", "").replace(" - Uncen", ""));
                }
                context.startActivity(animeDetails);
            });

        }
    }
}
