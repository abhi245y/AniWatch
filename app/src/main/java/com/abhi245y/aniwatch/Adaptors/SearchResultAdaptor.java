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
import com.abhi245y.aniwatch.datamodels.SearchViewModel;
import com.abhi245y.aniwatch.ui.AnimeDetailsActivity;
import com.bumptech.glide.Glide;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchViewModel searchViewModel = searchViewModels.get(position);
        Glide.with(context).load(searchViewModel.getPoster_link()).into(holder.poster_image);
        holder.result_name.setText(searchViewModel.getAnime_name());
        holder.total_eps.setText("Total Episodes: "+searchViewModel.getTotal_ep());

    }

    @Override
    public int getItemCount() {
        return searchViewModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView poster_image;
        public TextView result_name, total_eps;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            result_name = itemView.findViewById(R.id.result_name);
            poster_image = itemView.findViewById(R.id.poster_img);
            total_eps = itemView.findViewById(R.id.tEps);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SearchViewModel searchViewModel = searchViewModels.get(getAdapterPosition());
                    Intent intent = new Intent(context, AnimeDetailsActivity.class);
                    intent.putExtra("anime_name", searchViewModel.getGogo_id());
                    intent.putExtra("total_ep", String.valueOf(searchViewModel.getTotal_ep()));
                    intent.putExtra("current_ep", "1");
                    intent.putExtra("activity_name", "search_activity");
                    intent.putExtra("poster_link", searchViewModel.getPoster_link());
                    context.startActivity(intent);
                }
            });

        }
    }
}
