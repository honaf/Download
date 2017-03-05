package com.honaf.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.honaf.downloader.db.DBController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static com.honaf.downloader.DownloadEntry.DownloadStatus.completed;
import static com.honaf.downloader.DownloadEntry.DownloadStatus.paused;

/**
 * Created by honaf on 2016/10/26.
 */

public class DownloadService extends Service {
    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_CONNECTING = 5;
    //    1. net error 2. no sd 3. no memory
    public static final int NOTIFY_ERROR = 6;
    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap<>();
    private ExecutorService executorService;
    private LinkedBlockingDeque<DownloadEntry> mDownloadingDeque = new LinkedBlockingDeque<>();
    private DBController dbController;
    private DataChanger dataChanger;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("DownloadService==>", "onCreate");
        super.onCreate();
        executorService = Executors.newCachedThreadPool();
        dataChanger = DataChanger.getInstance(getApplicationContext());
        dbController = DBController.getInstance(getApplicationContext());
        ArrayList<DownloadEntry> downloadEntries = dbController.queryAll();
        if (downloadEntries != null) {
            for (DownloadEntry downloadEntry : downloadEntries) {
                if (downloadEntry.status == DownloadEntry.DownloadStatus.downloading || downloadEntry.status == DownloadEntry.DownloadStatus.waiting) {
                    downloadEntry.status = paused;
                    addDownload(downloadEntry);
                }
                dataChanger.addDBDataToDownloadEntrys(downloadEntry.id, downloadEntry);
            }
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntry tempDownloadEntry = (DownloadEntry) msg.obj;
            switch (msg.what) {
                case NOTIFY_PAUSED_OR_CANCELLED:
                case NOTIFY_COMPLETED:
                case NOTIFY_ERROR:
                    checkNext(tempDownloadEntry);
                    break;
//                case NOTIFY_COMPLETED:
//                    checkNext(tempDownloadEntry);
//                    mDownloadingTasks.remove(tempDownloadEntry.id);
//                    break;
            }
            dataChanger.postStatus((DownloadEntry) msg.obj);
        }
    };

    private void checkNext(DownloadEntry tempDownloadEntry) {
        DownloadEntry newDownloadEntry = mDownloadingDeque.poll();
        if (newDownloadEntry != null) {
            startDownload(newDownloadEntry);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("DownloadService==>", "onStartCommand");
        if (intent != null) {
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            DownloadEntry downloadEntry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            if(downloadEntry != null && dataChanger.containsDownloadEntryById(downloadEntry.id)) {
                downloadEntry = dataChanger.queryDownloadEntryById(downloadEntry.id);
            }
            doAction(action, downloadEntry);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry downloadEntry) {
        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                addDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
        }
    }

    private void recoverAll() {
        ArrayList<DownloadEntry> downloadEntries = dataChanger.queryAllPauseDownloadEntries();
        for (DownloadEntry entry : downloadEntries) {
            addDownload(entry);
        }
    }

    private void pauseAll() {
        while (mDownloadingDeque.iterator().hasNext()) {
            DownloadEntry entry = mDownloadingDeque.poll();
            entry.status = paused;
            Message msg = handler.obtainMessage();
            msg.obj = entry;
            handler.sendMessage(msg);
        }

        for (Map.Entry<String, DownloadTask> map : mDownloadingTasks.entrySet()) {
            map.getValue().pause();
        }
        mDownloadingTasks.clear();
    }

    private void pauseDownload(DownloadEntry downloadEntry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(downloadEntry.id);
        if (downloadTask != null) {
            downloadTask.pause();
        }
    }

    private void cancelDownload(DownloadEntry downloadEntry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(downloadEntry.id);
        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    private void addDownload(DownloadEntry downloadEntry) {
        if (mDownloadingTasks.size() >= 3) {
            mDownloadingDeque.add(downloadEntry);
            downloadEntry.status = DownloadEntry.DownloadStatus.waiting;
            dataChanger.postStatus(downloadEntry);
        } else {
            startDownload(downloadEntry);
        }
    }

    private void startDownload(DownloadEntry downloadEntry) {
        DownloadTask downloadTask = new DownloadTask(downloadEntry, handler,executorService);
        downloadTask.start();
        mDownloadingTasks.put(downloadEntry.id, downloadTask);
    }

}
