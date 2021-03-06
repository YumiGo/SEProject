package com.example.movie.ui.home;

import com.example.movie.MovieActivity;
import com.example.movie.data.Movie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movie.R;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private ArrayList<Movie> mList;
    private Context context;

    public MovieAdapter(Context context, ArrayList<Movie> list) {
        this.context = context;
        mList = list;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_movie_image, parent, false);

        MovieViewHolder viewHolder = new MovieViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        position = position % 10;
        if (mList == null || mList.size() == 0)
            return;
        if (position < 0 || position >= mList.size())
            return;

        Movie movie = mList.get(position);
        holder.movieImage.setImageBitmap(movie.getImage());

        // 영화 예약 액티비티 생성
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieActivity.class);
            intent.putExtra("id", movie.getId());
            intent.putExtra("name", movie.getName());
            intent.putExtra("director", movie.getDirector());
            intent.putExtra("content", movie.getContent());
            intent.putExtra("imageName", movie.getImageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return (mList == null ? 0 : Integer.MAX_VALUE);
    }

    @NonNull
    @Override
    public String toString() {
        return mList.toString();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        protected CardView cardView;
        protected ImageView movieImage;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            movieImage = itemView.findViewById(R.id.movieImage);
        }
    }
}
