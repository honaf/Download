package com.honaf.downloader;

import android.content.Context;
import android.content.Intent;

/**
 * Created by honaf on 2016/10/26.
 *
 */

public class DownloadManager {
    private static DownloadManager mInstance;
    private Context context;
    private static final int MIN_MUL_CLICK_INTERVAL = 1000;
    private long lastClickTime;

    private DownloadManager(Context context) {
        this.context = context;
    }

    public synchronized static DownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    public void add(DownloadEntry downloadEntry) {
        if (!checkMulClickExecute()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    public void pause(DownloadEntry downloadEntry) {
        if (!checkMulClickExecute()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    public void resume(DownloadEntry downloadEntry) {
        if (!checkMulClickExecute()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    public void cancel(DownloadEntry downloadEntry) {
        if (!checkMulClickExecute()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance().addObserver(dataWatcher);
    }

    public void removeObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance().deleteObserver(dataWatcher);
    }

    public void pauseAll() {
        if (!checkMulClickExecute()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        context.startService(intent);
    }

    public void recoverAll() {
        if (!checkMulClickExecute()) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        context.startService(intent);
    }

    private boolean checkMulClickExecute() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > MIN_MUL_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            return true;
        }
        return false;
    }
}
