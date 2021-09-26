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
import com.abhi245y.aniwatch.datamodels.AniApiRecentListModel;
import com.abhi245y.aniwatch.ui.AnimeDetailsActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecentReleasedAniViewAdaptor extends  RecyclerView.Adapter<RecentReleasedAniViewAdaptor.ViewHolder>{

    ArrayList<AniApiRecentListModel.RecentReleasesBean> recentReleasesBeans;
    Context context;
    String current_domain;

    @SuppressLint("NotifyDataSetChanged")
    public RecentReleasedAniViewAdaptor(ArrayList<AniApiRecentListModel.RecentReleasesBean> recentReleasesBeans, Context context, String current_domain) {
        this.recentReleasesBeans = recentReleasesBeans;
        this.context = context;
        this.current_domain = current_domain;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentReleasedAniViewAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentReleasedAniViewAdaptor.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_released_anime_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentReleasedAniViewAdaptor.ViewHolder holder, int position) {
        AniApiRecentListModel.RecentReleasesBean res = recentReleasesBeans.get(position);

        Glide.with(context).load(res.getImg_link()).placeholder(R.drawable.placeholder1).into(holder.aniCoverImg);
        holder.aniTitle.setText(res.getTitle());
        holder.aniEpisode.setText(res.getEp_num());

    }

    @Override
    public int getItemCount() {
        return recentReleasesBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView aniCoverImg;
        TextView aniTitle, aniEpisode;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            aniCoverImg = itemView.findViewById(R.id.ani_cover_img);
            aniTitle = itemView.findViewById(R.id.ani_title_tv);
            aniEpisode = itemView.findViewById(R.id.ani_released_ep_tv);

            itemView.setOnClickListener(view -> {
                AniApiRecentListModel.RecentReleasesBean recentReleasesBean = recentReleasesBeans.get(getAdapterPosition());
                Intent intent = new Intent(context, AnimeDetailsActivity.class);
                intent.putExtra("anime_name", recentReleasesBean.getTitle().toLowerCase().replaceAll("'","").replaceAll(":",""));
                intent.putExtra("activity_name", "recent_release");
                intent.putExtra("current_domain", current_domain);
                intent.putExtra("ep_link", recentReleasesBean.getEp_link());
                intent.putExtra("poster_link", recentReleasesBean.getImg_link());
                if(!recentReleasesBean.getEp_num().contains("Episode ")){
                    intent.putExtra("current_ep", "1");
                    intent.putExtra("total_ep", "1");

                }else{
                    intent.putExtra("current_ep", recentReleasesBean.getEp_num().replace("Episode ", "").replace(" - Uncen",""));
                    intent.putExtra("total_ep", recentReleasesBean.getEp_num().replace("Episode ","").replace(" - Uncen",""));
                }
                context.startActivity(intent);
            });

        }
    }
}
