package com.example.ni.popularmovies.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by ni on 2017/3/7.
 */

public class HttpMethodService extends IntentService {
    public static final String WAY_TO_RANK = "WAY_TO_RANK";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public HttpMethodService(){
        super("HttpMethodService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String wayToRank = intent.getStringExtra(WAY_TO_RANK);
        String action = intent.getAction();
        DataSaveTask.excuteTask(this,action,wayToRank); // 传入参数
    }
}
