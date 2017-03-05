package com.honaf.downloader;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import static com.honaf.downloader.Constants.MAX_DOWNLOAD_THREAD;

/**
 * Created by honaf on 2016/10/26.
 */

public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {
    private final DownloadEntry downloadEntry;
    private final Handler handler;
    private boolean isPaused;
    private boolean isCancelled;
    private ExecutorService executorService;
    private ConnectThread connectThread;
    private DownloadThread[] downloadThread;

    public DownloadTask(DownloadEntry downloadEntry, Handler handler, ExecutorService executorService) {
        this.downloadEntry = downloadEntry;
        this.handler = handler;
        this.executorService = executorService;
    }

    public void start() {
        if(downloadEntry.totalLength > 0){
            LogUtil.d("不需要connecting");
            startDownload();
        } else {
            downloadEntry.status = DownloadEntry.DownloadStatus.connecting;
            notifyHandler(DownloadService.NOTIFY_CONNECTING);
            connectThread = new ConnectThread(downloadEntry.url, this);
            executorService.execute(connectThread);
        }

    }

    private void startDownload() {
        LogUtil.d("startDownload");
        if (downloadEntry.enableRange) {
            startMulThreadDownload();
        } else {
            startSingleThreadDownload();
        }
    }

    public void notifyHandler(int what) {
        Message msg = handler.obtainMessage();
        msg.obj = downloadEntry;
        msg.what = what;
        handler.sendMessage(msg);
    }

    public void pause() {
        isPaused = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (downloadThread == null || downloadThread.length <= 0) {
            return;
        }
        for (int i = 0; i < downloadThread.length; i++) {
            if (downloadThread[i] != null && downloadThread[i].isRunning()) {
                downloadThread[i].pause();
            }
        }

    }

    public void cancel() {
        isCancelled = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (downloadThread == null || downloadThread.length <= 0) {
            return;
        }
        for (int i = 0; i < downloadThread.length; i++) {
            if (downloadThread[i] != null && downloadThread[i].isRunning()) {
                downloadThread[i].cancel();
            }
        }
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        LogUtil.d("onConnected");
        downloadEntry.enableRange = isSupportRange;
        downloadEntry.totalLength = totalLength;
        startDownload();
    }

    private void startMulThreadDownload() {
        downloadEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyHandler(DownloadService.NOTIFY_DOWNLOADING);
        long perSize = downloadEntry.totalLength / MAX_DOWNLOAD_THREAD;
        long startPosition;
        long endPosition;
        if (downloadEntry.progressMaps == null) {
            downloadEntry.progressMaps = new HashMap<>();
            for (int i = 0; i < MAX_DOWNLOAD_THREAD; i++) {
                downloadEntry.progressMaps.put(i, 0);
            }
        }
        downloadThread = new DownloadThread[MAX_DOWNLOAD_THREAD];
        for (int i = 0; i < MAX_DOWNLOAD_THREAD; i++) {
            startPosition = i * perSize + downloadEntry.progressMaps.get(i);
            if (i == MAX_DOWNLOAD_THREAD - 1) {
                endPosition = downloadEntry.totalLength - 1;
            } else {
                endPosition = (i + 1) * perSize - 1;
            }
            if (startPosition < endPosition) {
                downloadThread[i] = new DownloadThread(downloadEntry.url, i, startPosition, endPosition, this);
                executorService.execute(downloadThread[i]);
            }

        }

    }

    private void startSingleThreadDownload() {

    }

    @Override
    public synchronized void onConnectError(String message) {
        if (isPaused || isCancelled){
            downloadEntry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancel;
            notifyHandler(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
        } else {
            downloadEntry.status = DownloadEntry.DownloadStatus.error;
            notifyHandler(DownloadService.NOTIFY_ERROR);
        }

    }

    @Override
    public synchronized void onProgressChanged(int index, int progress) {
        downloadEntry.currentLength += progress;
        if (downloadEntry.currentLength == downloadEntry.totalLength) {
            downloadEntry.status = DownloadEntry.DownloadStatus.completed;
            notifyHandler(DownloadService.NOTIFY_COMPLETED);
            return;
        }
        int percent = (int) (downloadEntry.currentLength * 100L / downloadEntry.totalLength);
        if (percent > downloadEntry.percent) {
            downloadEntry.percent = percent;
            downloadEntry.progressMaps.put(index, downloadEntry.progressMaps.get(index) + progress);

            downloadEntry.status = DownloadEntry.DownloadStatus.downloading;
            notifyHandler(DownloadService.NOTIFY_UPDATING);
        }


    }

    @Override
    public synchronized void onDownloadComplete(int index) {

    }

    @Override
    public synchronized void onDownloadError(int index, String message) {
        LogUtil.d("onDownloadError");
        boolean isAllError = true;
        for (DownloadThread thread : downloadThread) {
            if(thread != null && !thread.isError()) {
                isAllError = false;
                LogUtil.d("出现下载异常，同时去取消其他线程");
                thread.cancelByError();
            }
        }
        if(isAllError) {
            downloadEntry.status = DownloadEntry.DownloadStatus.error;
            notifyHandler(DownloadService.NOTIFY_ERROR);
        }
    }


    @Override
    public synchronized void onDownloadPause(int index) {
        for (int i = 0; i < downloadThread.length; i++) {
            if(!downloadThread[i].isPause()) {
                return;
            }
        }
        downloadEntry.status = DownloadEntry.DownloadStatus.paused;
        notifyHandler(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadCancel(int index) {
        for (int i = 0; i < downloadThread.length; i++) {
            if(!downloadThread[i].isCancel()) {
                return;
            }
        }
        downloadEntry.status = DownloadEntry.DownloadStatus.cancel;
        downloadEntry.reset();
        String path = Environment.getExternalStorageDirectory() + File.separator
                + downloadEntry.url.substring(downloadEntry.url.lastIndexOf("/") + 1);
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }
        notifyHandler(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }
}
