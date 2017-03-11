package com.example.ni.popularmovies;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ni.popularmovies.adapter.MyDetailAdapter;
import com.example.ni.popularmovies.data.MovieContract;
import com.example.ni.popularmovies.bean.PopularMovie;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import common.ComUri;

/**
 * Created by ni on 2017/2/16.
 */

public class DetailFragment extends Fragment{
    public static final String MOVIE_DETAIL = "movie_detail";
    private String mUri;
    private RecyclerView mRecycleView;
    private View rootView;
    private Map<String,String> movieMap = new HashMap<>();
    private MyDetailAdapter adapter;

    private PopularMovie popularMovie;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }
    private static final String TAG = "Movie detail";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            popularMovie = arguments.getParcelable(DetailFragment.MOVIE_DETAIL);
        }

        rootView = inflater.inflate(R.layout.fragment_detail,container,false);
        mRecycleView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));//这里用线性显示 类似于list view
        mUri = ComUri.BASE_MOVIE_URL + String.valueOf(popularMovie.getMovieId()) + ComUri.COMMON_STR + BuildConfig.OPEN_MOVIE_KEY;
        adapter = new MyDetailAdapter(getActivity(),popularMovie,checkMarked(popularMovie));
        mRecycleView.setAdapter(adapter);

        return rootView;
    }

    /**
     * 该movie是否存在于数据库中，即
     * */
    private boolean checkMarked(PopularMovie movie) {
        Cursor cursor = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=? AND " + MovieContract.MovieEntry.COLUMN_MOVIE_MARKED + "=?",
                new String[] {String.valueOf(movie.getMovieId()),String.valueOf(PopularMovie.MARKED)},
                null);
        if (cursor.moveToNext()) {
            Log.i(TAG, "checkMarked: " + cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME)));
            movie.setMovieMarked(PopularMovie.MARKED);
            return true;
        }else {
            movie.setMovieMarked(PopularMovie.UNMARKED);
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }
    private void updateDetail(String uri) {
        MovieTask movieTask = new MovieTask();
        movieTask.execute(uri);
}

    public class MovieTask extends AsyncTask {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJson = null;

        @Override
        protected Object doInBackground(Object[] params) {

            try {
                URL url = new URL(mUri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + '\n');
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJson = buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return getMoiveDataFromJson(movieJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getMoiveDataFromJson(String movieJson) throws JSONException {


            JSONObject object = new JSONObject(movieJson);
            int runTime = object.getInt(ComUri.RUNTIME);
            Log.i(TAG, "getMoiveDataFromJson: " + runTime);
            popularMovie.setMovieRunTime(runTime);

            updateMovieTable(popularMovie);
            return null;

        }

        /**
         * 更新字段
         * */
        private void updateMovieTable(PopularMovie movie) {
            int id = movie.getMovieId();
            String stringId = Integer.toString(id);
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringId).build();
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_RUN_TIME, movie.getMovieRunTime());
            getActivity().getContentResolver().update(uri,cv, null,null);
        }

        @Override
        protected void onPostExecute(Object o) {
            adapter = new MyDetailAdapter(getActivity(),popularMovie,checkMarked(popularMovie));
            mRecycleView.setAdapter(adapter);
        }
    }
}
