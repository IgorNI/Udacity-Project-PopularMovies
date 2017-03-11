package com.example.ni.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ni.popularmovies.BuildConfig;
import com.example.ni.popularmovies.DetailActivity;
import com.example.ni.popularmovies.R;
import com.example.ni.popularmovies.bean.PopularMovie;
import com.squareup.picasso.Picasso;

import java.util.List;

import common.ComUri;

/**
 * Created by ni on 16-9-26.
 */

public class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyRecycleViewHolder> {
    private Context context;
    private List<String> mList;
    private LayoutInflater mLayoutInflater;
    private List<PopularMovie> mMovieList;
    String uri;

    static class MyRecycleViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;

        public MyRecycleViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageview);
        }
    }

    public MyRecycleViewAdapter(Context context, List<PopularMovie> mMovieList) {
        this.context = context;
        this.mMovieList = mMovieList;
        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public MyRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyRecycleViewHolder(mLayoutInflater.inflate(R.layout.movie_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyRecycleViewHolder holder, final int position) {

        Picasso.with(context).load(ComUri.BASE_IMAGE_URL + mMovieList.get(position).getMoviePostPath()).into(holder.imageView);
        Log.i("uri", "onBindViewHolder: " + ComUri.BASE_MOVIE_URL + String.valueOf(mMovieList.get(position).getMovieId()) + ComUri.REVIEWS + ComUri.COMMON_STR + BuildConfig.OPEN_MOVIE_KEY);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uri = ComUri.BASE_MOVIE_URL + String.valueOf(mMovieList.get(position).getMovieId()) + ComUri.COMMON_STR + BuildConfig.OPEN_MOVIE_KEY;
                Intent intent = new Intent();
                intent.putExtra("movie",mMovieList.get(position));
                intent.setClass(context,DetailActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

}
