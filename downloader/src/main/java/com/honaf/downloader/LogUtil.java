package com.honaf.downloader;

import android.util.Log;

/**
 * Created by honaf on 2016/10/26.
 */

public class LogUtil {
    public static final String tag = "honaf";

    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }

    }

    public static void e(String msg) {
//        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
//        }
    }
}
