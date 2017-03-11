package com.example.ni.popularmovies;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.ni.popularmovies.adapter.MyRecycleViewAdapter;
import com.example.ni.popularmovies.data.MovieContract;
import com.example.ni.popularmovies.bean.PopularMovie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import common.ComUri;

/**
 * Created by ni on 16-8-29.
 */
public class MovieShownFragment extends Fragment {

    private static final String TAG = "movieFragment";
    private int mPosition = RecyclerView.NO_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final String LAST_OFFSET = "last_offset";
    private static final String LAST_POSITION = "last_position";
    private RecyclerView recyclerView;
    private MyRecycleViewAdapter adapter;
    private View rootView;
    private SharedPreferences prefs;
    private String wayToRank;
    /* 为获取movieid而进行get */
    private static final int FLAG_GET_MOVIE_ID = 1;

    private static final int FLAG_GET_MOVIE_DETAILS = 4;

    private SharedPreferences.OnSharedPreferenceChangeListener preListener;
    boolean isChanged = false;

    public MovieShownFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.i(TAG, "onCreate: ");
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        wayToRank = prefs.getString(getString(R.string.rank_key),getString(R.string.hot_rated));
        preListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                isChanged = true;
                wayToRank = sharedPreferences.getString(key,getString(R.string.hot_rated));
                Log.i(TAG, "onSharedPreferenceChanged: " + wayToRank);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(preListener);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClass(getActivity(),SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.action_refresh:
                Log.i(TAG, "onOptionsItemSelected: ");
                updateMovie();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        movieList = new ArrayList<>();
        rootView = inflater.inflate(R.layout.fragment_movie,container,false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycleView);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.i("position", "dx: " + dx + " dy: " + dy);
                getPositionAndOffset();
            }
        });
        if (savedInstanceState != null ) {
            Log.i(TAG, "savedInsatance");
        }
        return rootView;
    }

    private int lastPosition;
    private int lastOffset;
    private void getPositionAndOffset() {
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        View topView = layoutManager.getChildAt(0);
        if(topView != null) {
            //获取与该view的顶部的偏移量
            lastOffset = topView.getTop();
            //得到该View的数组位置
            lastPosition = layoutManager.getPosition(topView);
            Log.i("position", "lastOffset: " + lastOffset + " lastposition: " + lastPosition);
        }
    }

    /**
     * 让RecyclerView滚动到指定位置
     */
    private void scrollToPosition() {
        if(recyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((GridLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getTag();
        Log.i(TAG, "onViewCreated: ");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: ");
        if (savedInstanceState != null ) {
            lastOffset = savedInstanceState.getInt(LAST_OFFSET);
            lastPosition = savedInstanceState.getInt(LAST_POSITION);
            Log.i(TAG, "lastOffset: " + lastOffset + " lastposition: " + lastPosition);
        }
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onstart" + movieList.size());
        super.onStart();
        if (movieList.isEmpty() || isChanged) {
            updateMovie();
            isChanged = false;
        }else {
            Log.i(TAG, "nothing changed");
        }

    }

    /**
     * 判断是否联网
     * */
    private boolean isOnline() {
        Log.i("movie", "isOnline:");
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    /**
     * 获取电影
     * */
    public void updateMovie() {
        movieList.clear();
        FetchMovieTask task = new FetchMovieTask();
        task.execute(wayToRank);

    }



    private String uri;
    private List<PopularMovie> movieList;
    /**获取json对象，并获得图片的poster path*/
    private class FetchMovieTask extends AsyncTask<String, Void, String[]>{
        @Override
        protected String[] doInBackground(String... string) {
            if (isOnline()) {

                if (!string[0].equals(getString(R.string.favorites))) {
                    // 如果偏好设置不是favorite;
                    uri = ComUri.BASE_MOVIE_URL + string[0] + ComUri.COMMON_STR + BuildConfig.OPEN_MOVIE_KEY;
                    httpRequestMethod(uri,FLAG_GET_MOVIE_ID);
                }else {
                    // 如果偏好设置是favorite,则显示数据库中的movie
                    return queryAllMarkedMovie();
                }
                return null;
            }else {
                queryLocalMovie(string[0]);
                return null;
            }
        }


        @Override
        protected void onPostExecute(String[] strings) {
            adapter = new MyRecycleViewAdapter(getActivity(),movieList);
//
            recyclerView.setAdapter(adapter);
        }
    }

    private String[] httpRequestMethod(String uri,int flag) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJsonStr = null;
        try {
            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // nothing to do
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
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
            return getMoiveIdFromJson(movieJsonStr,flag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void queryLocalMovie(String str) {
        if (str.equals(getString(R.string.hot_rated))) {
            // TODO: 2017/3/3 将本地的电影按照评分从高到低排列
            queryMovie(str);
        }else if (str.equals(getString(R.string.popular))) {
            // TODO: 2017/3/3 将本地电影按照受欢迎程度从高到低排列
            queryMovie(str);
        }else {
            // // TODO: 2017/3/3 显示收藏的电影
            queryAllMarkedMovie();
        }
    }
    /**
     * 获取收藏的电影
     * */
    public String[] queryAllMarkedMovie() {
        Cursor cursor = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,null,MovieContract.MovieEntry.COLUMN_MOVIE_MARKED + "=?", new String[] {String.valueOf(PopularMovie.MARKED)},null);
        String[] result = new String[cursor.getCount()];
        while (cursor.moveToNext()) {
            PopularMovie movie = new PopularMovie();
            String path = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH));
            result[cursor.getPosition()] = path;
            String overView = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW));
            String movieTitle = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
            double vote = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE));
            double popularity = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY));
            String date = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_DATE));
            int movieId = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
            int runTime = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RUN_TIME)); // 电影时长
            String trailersJson = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS));
            String reviewsJson = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_REVIEWS));

            movie.setVideosJson(trailersJson);
            movie.setReviewsJson(reviewsJson);
            movie.setMovieRunTime(runTime);
            movie.setMovieDate(date);
            movie.setMovieId(movieId);
            movie.setMovieName(movieTitle);
            movie.setMovieOverview(overView);
            movie.setMovieVote(vote);
            movie.setMoviePopularity(popularity);
            movie.setMoviePostPath(path);
            movieList.add(movie);
        }
        cursor.close();
        return result;
    }

    /**
     * 根据所选字段，显示相应的电影
     * @param str : 所选字段
     * */
    public List<PopularMovie> queryMovie(String str) {
        String sortOrder = null;
        if (str.equals(getString(R.string.hot_rated))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_VOTE + " DESC";
        }else if (str.equals(getString(R.string.popular))) {
            // TODO: 2017/3/3 将本地电影按照受欢迎程度从高到低排列
            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY + " DESC";
        }
        Cursor cursor = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,null,null,null, sortOrder);
        while (cursor.moveToNext()) {
            PopularMovie movie = new PopularMovie();
            String path = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH));
            String overView = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW));
            String movieTitle = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
            double vote = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE));
            double popularity = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY));
            String date = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_DATE));
            int movieId = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
            int runTime = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RUN_TIME)); // 电影时长
            String trailersJson = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS));
            String reviewsJson = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_REVIEWS));

            movie.setVideosJson(trailersJson);
            movie.setReviewsJson(reviewsJson);
            movie.setMovieRunTime(runTime);
            movie.setMovieDate(date);
            movie.setMovieId(movieId);
            movie.setMovieName(movieTitle);
            movie.setMovieOverview(overView);
            movie.setMovieVote(vote);
            movie.setMoviePopularity(popularity);
            movie.setMoviePostPath(path);

            movieList.add(movie);
        }
        cursor.close();
        return movieList;
    }

    private String[] getMoiveIdFromJson(String movieJsonStr,int flag) throws JSONException {
        // 获取json对象
        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieTrailers = null;
        JSONArray movieReviews = null;
        if (movieJson.has("videos") && movieJson.getJSONObject("videos").has("results")) {
            movieTrailers = movieJson.getJSONObject("videos").getJSONArray("results");
        }
        if (movieJson.has("reviews") && movieJson.getJSONObject("reviews").has("results")) {
            movieReviews = movieJson.getJSONObject("reviews").getJSONArray("results");
        }
        if (flag == FLAG_GET_MOVIE_ID) {
            // 获取json对象中的“results”数组
            JSONArray movieArray = movieJson.getJSONArray(ComUri.JSON_RESULTS);

            // 默认返回20条数据
            String results[] = new String[20];
            // 遍历json数组
            for (int i = 0;i < movieArray.length();i++) {
                PopularMovie movie = new PopularMovie();
                JSONObject oneMovieInfo = movieArray.getJSONObject(i);
                // 获取图片的路径 poster_path;

                int movieId = oneMovieInfo.getInt(ComUri.JSON_MOVIE_ID);
                getMovieDataById(movieId,FLAG_GET_MOVIE_DETAILS);
            }
        }else if (flag == FLAG_GET_MOVIE_DETAILS) {
            PopularMovie movie = new PopularMovie();
            String poseterPath = movieJson.getString(ComUri.JSON_POSTER_PATH); // 图片地址
            String overView = movieJson.getString(ComUri.JSON_OVER_VIEW); // 简介
            String movieTitle = movieJson.getString(ComUri.JSON_ORIGINAL_TITLE); // 电影名称
            double vote = movieJson.getDouble(ComUri.JSON_VOTE_AVERAGE); // 评分
            double popularity = movieJson.getDouble(ComUri.JSON_MOVIE_POPULARITY); // 欢迎程度
            String date = movieJson.getString(ComUri.JSON_DATE); // 日期
            int runTime = movieJson.getInt(ComUri.JSON_MOVIE_RUNTIME); // 电影时长
            int movieId = movieJson.getInt(ComUri.JSON_MOVIE_ID);
            movie.setMovieDate(date);
            movie.setMovieId(movieId);
            movie.setMovieName(movieTitle);
            movie.setMovieOverview(overView);
            movie.setMovieVote(vote);
            movie.setMoviePopularity(popularity);
            movie.setMoviePostPath(poseterPath);
            movie.setMovieRunTime(runTime);
            movie.setVideosJson(movieTrailers.toString());
            movie.setReviewsJson(movieReviews.toString());
            movieList.add(movie);
            return null;
        }
        checkToExists(movieList);
        return null;
    }

    private void getMovieDataById(int movieId,int flag) {
        String uri = ComUri.BASE_MOVIE_URL + String.valueOf(movieId) + ComUri.COMMON_STR + BuildConfig.OPEN_MOVIE_KEY + "&append_to_response=videos,reviews";
        httpRequestMethod(uri,flag);
    }

    /**
     * 获取movie_runtime
     * @param movieJsonStr 得到的json数据
     * */
    private String[] getRunTime(String movieJsonStr) throws JSONException {
        JSONObject object = new JSONObject(movieJsonStr);
        int runTime = object.getInt(ComUri.RUNTIME);
        Log.i("movie", "getRunTime: " + runTime);
        return new String[] {String.valueOf(runTime)};
    }


    /**
     * 判断数据库中是否有该电影
     * @param movieList
     * */
    private void checkToExists(List<PopularMovie> movieList) {
        List<ContentValues> list = new ArrayList<>();
        Cursor cursor = null;
        for (int i = 0; i < movieList.size(); i++) {
            PopularMovie movie = movieList.get(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cursor = getContext().getContentResolver().query(
                        MovieContract.MovieEntry.CONTENT_URI,null, MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{String.valueOf(movie.getMovieId())},null);
            }
            if (cursor.moveToNext()) {
                cursor.close();
            }else {
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_NAME, movie.getMovieName());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE, movie.getMovieVote());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_DATE, movie.getMovieDate());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_MARKED, PopularMovie.UNMARKED);
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getMovieOverview());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY, movie.getMoviePopularity());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH, movie.getMoviePostPath());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_RUN_TIME, movie.getMovieRunTime());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS, movie.getVideosJson());
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_REVIEWS, movie.getReviewsJson());
                list.add(cv);
                cursor.close();
            }
        }
        ContentValues[] cv = new ContentValues[list.size()];
        Log.i(TAG, "checkToExists: " + list.size());
        getActivity().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, list.toArray(cv));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("movie", "onSaveInstanceState: ");
        outState.putInt(LAST_OFFSET,lastOffset);
        outState.putInt(LAST_POSITION,lastPosition);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
        scrollToPosition();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
