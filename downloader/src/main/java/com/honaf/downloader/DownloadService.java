package com.honaf.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by honaf on 2016/10/26.
 */

public class DownloadService extends Service{
    private HashMap<String,DownloadTask> mDownloadingTasks = new HashMap<>();
    private ExecutorService executorService;
    private LinkedBlockingDeque<DownloadEntry> mDownloadingDeque = new LinkedBlockingDeque<>();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("DownloadService==>","onCreate");
        super.onCreate();
        executorService = Executors.newCachedThreadPool();
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntry tempDownloadEntry = (DownloadEntry) msg.obj;
            switch (tempDownloadEntry.status) {
                case cancel:
                case paused:
                    checkNext(tempDownloadEntry);
                    break;
                case completed:
                    checkNext(tempDownloadEntry);
                    mDownloadingTasks.remove(tempDownloadEntry.id);
                    break;
            }
            DataChanger.getInstance().postStatus((DownloadEntry) msg.obj);
        }
    };

    private void checkNext(DownloadEntry tempDownloadEntry) {
        DownloadEntry newDownloadEntry = mDownloadingDeque.poll();
        if(newDownloadEntry != null) {
            startDownload(newDownloadEntry);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION,-1);
        DownloadEntry downloadEntry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
        doAction(action,downloadEntry);
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
        ArrayList<DownloadEntry> downloadEntries = DataChanger.getInstance().queryAllPauseDownloadEntries();
        for (DownloadEntry entry : downloadEntries) {
            addDownload(entry);
        }
    }

    private void pauseAll() {
        while(mDownloadingDeque.iterator().hasNext()) {
            DownloadEntry entry = mDownloadingDeque.poll();
            entry.status = DownloadEntry.DownloadStatus.paused;
            Message msg = handler.obtainMessage();
            msg.obj = entry;
            handler.sendMessage(msg);
        }

        for (Map.Entry<String,DownloadTask> map : mDownloadingTasks.entrySet() ) {
            map.getValue().pause();
        }
        mDownloadingTasks.clear();
    }

    private void pauseDownload(DownloadEntry downloadEntry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(downloadEntry.id);
        if(downloadTask != null) {
            downloadTask.pause();
        }
    }

    private void cancelDownload(DownloadEntry downloadEntry) {
        DownloadTask downloadTask = mDownloadingTasks.remove(downloadEntry.id);
        if(downloadTask != null) {
            downloadTask.cancel();
        }
    }
    private void addDownload(DownloadEntry downloadEntry) {
        if(mDownloadingTasks.size() >= 3) {
            mDownloadingDeque.add(downloadEntry);
            downloadEntry.status = DownloadEntry.DownloadStatus.waiting;
            DataChanger.getInstance().postStatus(downloadEntry);
        }else {
            startDownload(downloadEntry);
        }
    }
    private void startDownload(DownloadEntry downloadEntry) {
        DownloadTask downloadTask = new DownloadTask(downloadEntry,handler);
//        downloadTask.start();
        mDownloadingTasks.put(downloadEntry.id,downloadTask);
        executorService.execute(downloadTask);
    }

}
