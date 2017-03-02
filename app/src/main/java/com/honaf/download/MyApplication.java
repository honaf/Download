package com.honaf.download;

import android.app.Application;
import android.util.Log;

/**
 * Created by honaf on 2017/3/2.
 */

public class MyApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MyApplication==>","onCreate");
//        DownloadManager.getInstance(this);
    }
}
