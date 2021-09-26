package com.abhi245y.aniwatch.Adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi245y.aniwatch.R;
import com.abhi245y.aniwatch.datamodels.EpisodesListRVDataModel;

import java.util.ArrayList;

public class VideoListRvAdaptor extends RecyclerView.Adapter<VideoListRvAdaptor.ViewHolder>{

    ArrayList<EpisodesListRVDataModel> episodesListRVDataModelArrayList;
    Context context;

    public VideoListRvAdaptor(ArrayList<EpisodesListRVDataModel> episodesListRVDataModelArrayList, Context context) {
        this.episodesListRVDataModelArrayList = episodesListRVDataModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.episodes_view_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EpisodesListRVDataModel res = episodesListRVDataModelArrayList.get(position);
        String epNum = "Episode "+res.getEpNum();
        holder.title.setText(epNum);
        holder.thump.setImageBitmap(res.getThumpBit());

//        Glide.with(context).asBitmap().load(res.getUrl()).into(holder.thump);

    }

    @Override
    public int getItemCount() {
        return episodesListRVDataModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thump;
        TextView title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thump = itemView.findViewById(R.id.thump);
            title  = itemView.findViewById(R.id.vidTitle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    MediaItem mediaItem = MediaItem.fromUri(episodesListRVDataModelArrayList.get(getAbsoluteAdapterPosition()).getUrl());
//                    simpleExoPlayer.addMediaItem(mediaItem);
//                    simpleExoPlayer.prepare();
                }
            });
        }
    }


}
