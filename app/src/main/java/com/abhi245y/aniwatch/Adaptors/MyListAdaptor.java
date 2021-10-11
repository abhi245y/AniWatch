package com.abhi245y.aniwatch.Adaptors;

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
import com.abhi245y.aniwatch.ui.AnimeDetailsActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyListAdaptor extends RecyclerView.Adapter<MyListAdaptor.ViewHolder>{


    private final ArrayList<AnimeMongo> myListModels;
    Context context;

    public MyListAdaptor(ArrayList<AnimeMongo> myListModels, Context context) {
        this.myListModels = myListModels;
        this.context = context;
    }

    @NonNull
    @Override
    public MyListAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyListAdaptor.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_released_anime_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyListAdaptor.ViewHolder holder, int position) {
        AnimeMongo res = myListModels.get(position);
        Glide.with(context).load(res.getImgLink()).centerCrop().placeholder(R.drawable.placeholder1).into(holder.aniCoverImg);
        holder.aniTitle.setText(res.getTitleName());
    }

    @Override
    public int getItemCount() {
        return myListModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView aniCoverImg;
        TextView aniTitle, aniEpisode;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            aniCoverImg = itemView.findViewById(R.id.ani_cover_img);
            aniTitle = itemView.findViewById(R.id.ani_title_tv);
            aniTitle.setSelected(true);
            aniEpisode = itemView.findViewById(R.id.ani_released_ep_tv);
            aniEpisode.setVisibility(View.GONE);

            itemView.setOnClickListener(view -> {
                AnimeMongo animeMongo = myListModels.get(getBindingAdapterPosition());
                Intent animeDetails = new Intent(context, AnimeDetailsActivity.class);
                animeDetails.putExtra("animeData", animeMongo);
                animeDetails.putExtra("current_ep", "1");
                context.startActivity(animeDetails);
            });

        }
    }
}
