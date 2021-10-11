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
import com.abhi245y.aniwatch.datamodels.SearchViewModel;
import com.abhi245y.aniwatch.ui.AnimeDetailsActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class SearchResultAdaptor extends RecyclerView.Adapter<SearchResultAdaptor.ViewHolder> {

    private final ArrayList<SearchViewModel> searchViewModels;
    Context context;

    @SuppressLint("NotifyDataSetChanged")
    public SearchResultAdaptor(ArrayList<SearchViewModel> searchViewModels, Context context) {
        this.searchViewModels = searchViewModels;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_rv_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimeMongo animeMongo = searchViewModels.get(position).getAnimeMongo();
        Glide.with(context).load(animeMongo.getImgLink()).fitCenter().into(holder.poster_image);
        String titleName = animeMongo.getTitleName();
        holder.result_name.setText(titleName);
        String released = "Released: "+animeMongo.getReleased();
        if(animeMongo.getOtherNamesList().size()>2) {
            holder.otherNames.setText(animeMongo.getOtherNamesList().get(0));
        }
        holder.release_year.setText(released);

        String genres =  animeMongo.getGenre().toString().replace("[","").replace("]","");
        String otherNames =  animeMongo.getOtherNames().replace("[","").replace("]","");
        holder.genresTv.setText(genres);
        holder.otherNames.setText(otherNames);

//        for(int i = 0; i<=5; i++){
//            if (i<animeMongo.getGenre().size()) {
//                String genre = animeMongo.getGenre().get(i);
//                Chip chip = new Chip(context);
//                chip.setText(genre);
//                holder.genreChipGroup.addView(chip);
//            }
//        }

    }

    @Override
    public int getItemCount() {
        return searchViewModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView poster_image;
        public TextView result_name, release_year, otherNames, genresTv;
        public ChipGroup genreChipGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            result_name = itemView.findViewById(R.id.result_name);
            poster_image = itemView.findViewById(R.id.poster_img);
            release_year = itemView.findViewById(R.id.release_date);
            genreChipGroup = itemView.findViewById(R.id.genreChipGroup);
            otherNames = itemView.findViewById(R.id.other_names);
            genresTv = itemView.findViewById(R.id.genres);

            itemView.setOnClickListener(view -> {
                SearchViewModel searchViewModel = searchViewModels.get(getBindingAdapterPosition());
                Intent animeDetails = new Intent(context, AnimeDetailsActivity.class);
                animeDetails.putExtra("animeData", searchViewModel.getAnimeMongo());
                animeDetails.putExtra("current_ep", "1");
                context.startActivity(animeDetails);
            });

        }
    }
}
