package com.example.ni.popularmovies.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by ni on 2017/3/7.
 * 用于解析json，存储数据
 */

public class DataSaveTask {
    public static final String ACTION_GET_DATA_FROM_JSON = "action_get_data_from_json";


    public static void excuteTask(Context context, String action,String wayToRank) {
        if (action.equals(ACTION_GET_DATA_FROM_JSON)) {
            getDataFromJson(context,wayToRank);
        }
    }

    private static void getDataFromJson(Context context, String wayToRank) {

    }

    /**
     * 判断是否联网
     * */
    private boolean isOnline(Context context) {
        Log.i("movie", "isOnline:");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

}
