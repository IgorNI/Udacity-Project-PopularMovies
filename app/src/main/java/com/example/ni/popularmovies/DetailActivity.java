package com.example.ni.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

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
 * Created by ni on 16-8-18.
 */
public class DetailActivity extends ActionBarActivity{

    private String uri;
    private Map<String,String> movieMap = new HashMap<>();
    private PopularMovie popularMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            DetailFragment detailFragment = new DetailFragment();
            popularMovie = getIntent().getParcelableExtra("movie");
            Bundle argument = new Bundle();
            argument.putParcelable(DetailFragment.MOVIE_DETAIL,popularMovie);

            detailFragment.setArguments(argument);

//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.weather_detail_container, detailFragment)
//                    .commit();
            getFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container,detailFragment)
                    .commit();

        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }
    private void updateDetail(String uri) {
        MovieTask movieTask = new MovieTask();
        movieTask.execute(uri);
    }

    public class MovieTask extends AsyncTask{

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJson = null;
        @Override
        protected Map<String,String> doInBackground(Object[] params) {
            try {
                URL url = new URL(uri);
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
            }finally {
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

        private Map<String,String> getMoiveDataFromJson(String movieJson) throws JSONException{


            JSONObject object = new JSONObject(movieJson);
            String name = object.getString(ComUri.TITLE);
            String poster_path = object.getString(ComUri.POSTER_PATH);
            String overView = object.getString(ComUri.OVERVIEW);
            String vote = object.getString(ComUri.VOTE);
            String date = object.getString(ComUri.DATE);

            // TODO: 2017/3/1 将解析出得字段存入Movie中，
            movieMap.put(ComUri.TITLE,name);
            movieMap.put(ComUri.POSTER_PATH,poster_path);
            movieMap.put(ComUri.OVERVIEW,overView);
            movieMap.put(ComUri.VOTE,vote);
            movieMap.put(ComUri.DATE,date);
            return movieMap;

        }

        @Override
        protected void onPostExecute(Object o) {;

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(DetailActivity.this, SettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
