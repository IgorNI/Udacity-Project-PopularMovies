package com.example.ni.popularmovies.adapter;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ni.popularmovies.BuildConfig;
import com.example.ni.popularmovies.R;
import com.example.ni.popularmovies.bean.MovieReviews;
import com.example.ni.popularmovies.bean.MovieTrailers;
import com.example.ni.popularmovies.data.MovieContract;
import com.example.ni.popularmovies.bean.PopularMovie;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import common.ComUri;

/**
 * Created by ni on 2017/2/16.
 */

public class MyDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private PopularMovie popularMovie;
    private static final int ITEM_TYPE_DETIAL = 0; // 详情页
    private static final int ITEM_TYPE_TRAILERS = 1; // 预告片
    private static final int ITEM_TYPE_COMMECT = 2; // 评论页
    private boolean isMarked = false;
    private LayoutInflater mLayoutInflater;
    public static final String TAG = "MyDetailAdapter";
    private List<MovieTrailers> trailersList = new ArrayList<>();
    private List<MovieReviews> reviewsList = new ArrayList<>();
    private static final int UPDATE_MARKED_STATUE = 3;


    public MyDetailAdapter(Context context,PopularMovie movie, boolean isMarked) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        popularMovie = movie;
        this.isMarked = isMarked;
        getVideoAndReviews(popularMovie);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_DETIAL) {
            return new DetailViewHolder(mLayoutInflater.inflate(R.layout.item_movieinfo_layout,parent,false));
        }else if (viewType == ITEM_TYPE_TRAILERS) {
            Log.i(TAG, "onCreateViewHolder: " + ITEM_TYPE_TRAILERS);
//            return new TrailersViewHolder(R.layout.rv_movie_trailers,parent,viewType);
            return new TrailersViewHolder(mLayoutInflater.inflate(R.layout.rv_movie_trailers,parent,false));
        }else
            Log.i(TAG, "onCreateViewHolder: " + ITEM_TYPE_COMMECT);
            return new ReviewsViewHolder(mLayoutInflater.inflate(R.layout.rv_movie_reviews,parent,false));

    }

    private void getVideoAndReviews(PopularMovie popularMovie) {
        getVideo(popularMovie);
        getReviews(popularMovie);
    }

    private void getVideo(PopularMovie popularMovie) {
        String videoJson = popularMovie.getVideosJson();
        if (videoJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(videoJson);
                for (int i = 0;i < jsonArray.length();i++) {
                    MovieTrailers trailers = new MovieTrailers();
                    JSONObject obj = jsonArray.getJSONObject(i);
                    trailers.name = obj.getString("name");
                    trailers.key = obj.getString("key");
                    trailersList.add(trailers);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getReviews(PopularMovie popularMovie) {
        String reviewJson = popularMovie.getReviewsJson();
        if (reviewJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(reviewJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    MovieReviews reviews = new MovieReviews();
                    JSONObject reviewsJson = jsonArray.getJSONObject(i);
                    reviews.author = reviewsJson.getString("author");
                    reviews.content = reviewsJson.getString("content");
                    reviews.urlStr = reviewsJson.getString("url");
                    reviewsList.add(reviews);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case ITEM_TYPE_DETIAL:
                    // 详情页
                    Picasso.with(mContext).load(ComUri.BASE_IMAGE_URL + popularMovie.getMoviePostPath()).into(((DetailViewHolder) holder).imageView);
                    ((DetailViewHolder) holder).mTitleTv.setText(popularMovie.getMovieName());
                    ((DetailViewHolder) holder).mDateTv.setText(popularMovie.getMovieDate());
                    ((DetailViewHolder) holder).mRatedTv.setText(String.valueOf(popularMovie.getMovieVote()));
                    ((DetailViewHolder) holder).mOverViewTv.setText(popularMovie.getMovieOverview());
                    ((DetailViewHolder) holder).mRunTimeTv.setText(String.valueOf(popularMovie.getMovieRunTime()));
                    final Button btn = ((DetailViewHolder) holder).mMarkBtn;
                    if (isMarked) {
                        btn.setText("已收藏");
                    }else {
                        btn.setText("未收藏");
                    }
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isMarked) {
                                QueryHandler queryHandler = new QueryHandler(mContext.getContentResolver());
                                int id = popularMovie.getMovieId();
                                String stringId = Integer.toString(id);
                                Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                                uri = uri.buildUpon().appendPath(stringId).build();
                                ContentValues cv = new ContentValues();
                                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_MARKED, PopularMovie.MARKED);
                                queryHandler.startUpdate(UPDATE_MARKED_STATUE,null,uri,cv,null,null);
//                                mContext.getContentResolver().update(uri,cv, null,null);
                                popularMovie.setMovieMarked(PopularMovie.MARKED);
                                btn.setText("已收藏");
                            }else {
                                // 取消收藏，删除数据库重的数据，
                                QueryHandler queryHandler = new QueryHandler(mContext.getContentResolver());
                                int id = popularMovie.getMovieId();
                                String stringId = Integer.toString(id);
                                Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                                uri = uri.buildUpon().appendPath(stringId).build();
                                ContentValues cv = new ContentValues();
                                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_MARKED, PopularMovie.UNMARKED);
                                queryHandler.startUpdate(UPDATE_MARKED_STATUE,null,uri,cv,null,null);
//                                mContext.getContentResolver().update(uri,cv,null, null);
                                btn.setText("未收藏");
                            }
                        }
                    });
                    break;
                case ITEM_TYPE_TRAILERS:
                    // 预告片
                    ((TrailersViewHolder)holder).refreshData(trailersList,position);
                    break;
                case ITEM_TYPE_COMMECT:
                    // 评论页
                    ((ReviewsViewHolder)holder).refreshData(reviewsList,position);
            }
            }

    private final class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            super.onUpdateComplete(token, cookie, result);
            Log.i(TAG, "onUpdateComplete: ");
        }
    }


    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        int i = position % 3;
        if (i == 0) {
            return ITEM_TYPE_DETIAL; // 详情页
        }else if (i == 1) {
            return ITEM_TYPE_TRAILERS; // 预告片
        }else {
            return ITEM_TYPE_COMMECT; // 评论
        }
    }

    /**
     * 详情页的viewholder
     * */
     static class DetailViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView mTitleTv;
        private TextView mDateTv;
        private TextView mRatedTv;
        private TextView mOverViewTv;
        private Button mMarkBtn;
        private TextView mRunTimeTv;
        public DetailViewHolder(View itemView) {
            super(itemView);
            imageView= (ImageView) itemView.findViewById(R.id.iv_post_view);
            mOverViewTv = (TextView) itemView.findViewById(R.id.tv_overView);
            mTitleTv = (TextView) itemView.findViewById(R.id.tv_title);
            mDateTv = (TextView) itemView.findViewById(R.id.tv_relate_date);
            mRatedTv = (TextView) itemView.findViewById(R.id.tv_rate);
            mMarkBtn = (Button) itemView.findViewById(R.id.mark_btn);
            mRunTimeTv = (TextView) itemView.findViewById(R.id.tv_runtime);
        }
    }



        class TrailersViewHolder extends RecyclerView.ViewHolder {

            private List<MovieTrailers> tralist;
            private RecyclerView mRecycleView;
            public TrailersViewHolder(View itemView) {
                super(itemView);
                mRecycleView = (RecyclerView) itemView.findViewById(R.id.recycleview_trailers);
            }

            public void refreshData(List<MovieTrailers> tralist, int position) {
                this.tralist = tralist;
                mRecycleView.setLayoutManager(new LinearLayoutManager(mContext));
                mRecycleView.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mRecycleView.setAdapter(new TrailersAdapter(mLayoutInflater));
            }

            private class TrailersAdapter extends RecyclerView.Adapter<TrailersHolder> {
                private LayoutInflater inflate;
                public TrailersAdapter(LayoutInflater inflate) {
                    this.inflate = inflate;
                }
                @Override
                public TrailersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return new TrailersHolder(inflate.inflate(R.layout.item_trailers_layout,parent,false));
                }

                @Override
                public void onBindViewHolder(TrailersHolder holder, final int position) {
                    holder.mTv.setText(tralist.get(position).name);
                    holder.mIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            Uri vedioUri = Uri.parse("https://www.youtube.com/watch?v=" + tralist.get(position).key);
                            intent.setData (vedioUri);
//                            startActivity(intent);
                            mContext.startActivity(intent);
                        }
                    });
                }

                @Override
                public int getItemCount() {
                    return tralist.size();
                }
            }


            private class TrailersHolder extends RecyclerView.ViewHolder {
                private ImageView mIv;
                private TextView mTv;


                public TrailersHolder(View itemView) {
                    super(itemView);
                    mTv = (TextView) itemView.findViewById(R.id.tv_trailers);
                    mIv = (ImageView) itemView.findViewById(R.id.iv_play_trailers);
                }
            }
        }


     class ReviewsViewHolder extends RecyclerView.ViewHolder {

         private RecyclerView mRecycleView;
         private List<MovieReviews> mList;

        public ReviewsViewHolder(View itemView) {
            super(itemView);
            mRecycleView = (RecyclerView) itemView.findViewById(R.id.recycleview_reviews);
        }

         public void refreshData(List<MovieReviews> reviewsList, int position) {
             this.mList = reviewsList;
             mRecycleView.setLayoutManager(new LinearLayoutManager(mContext));
             mRecycleView.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
             mRecycleView.setAdapter(new ReviewsViewHolder.ReviewsAdapter(mLayoutInflater));
         }

         private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsHolder> {
             private LayoutInflater inflate;
             public ReviewsAdapter(LayoutInflater mLayoutInflater) {
                 this.inflate = mLayoutInflater;
             }

             @Override
             public ReviewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                 return new ReviewsHolder(inflate.inflate(R.layout.item_reviews_layout,parent,false));
             }

             @Override
             public void onBindViewHolder(ReviewsHolder holder, final int position) {
                 holder.mAuthorTv.setText(reviewsList.get(position).author);
                 holder.mContentTv.setText(reviewsList.get(position).content);
                 holder.mContentTv.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         View dialogView = View.inflate(mContext, R.layout.dialog_review_details, null);
                         TextView tvDialogContent = (TextView) dialogView.findViewById(R.id.dialog_content_tv);
                         tvDialogContent.setText(reviewsList.get(position).content);
                         AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                                 .setTitle("Review Details")
                                 .setCancelable(true)
                                 .setView(dialogView)
                                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         dialog.cancel();
                                     }
                                 });
                         builder.show();
                     }
                 });
             }

             @Override
             public int getItemCount() {
                 return reviewsList.size();
             }
         }

         private class ReviewsHolder extends RecyclerView.ViewHolder {
             private TextView mAuthorTv;
             private TextView mContentTv;
             public ReviewsHolder(View itemView) {
                 super(itemView);
                 mAuthorTv = (TextView) itemView.findViewById(R.id.review_author_tv);
                 mContentTv = (TextView) itemView.findViewById(R.id.review_content_tv);
             }
         }
     }
}
