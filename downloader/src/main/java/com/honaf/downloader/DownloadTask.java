package com.honaf.downloader;

import android.os.Handler;
import android.os.Message;

/**
 * Created by honaf on 2016/10/26.
 */

public class DownloadTask implements Runnable{
    private final DownloadEntry downloadEntry;
    private final Handler handler;
    private boolean isPaused;
    private boolean isCancelled;

    public DownloadTask(DownloadEntry downloadEntry, Handler handler) {
        this.downloadEntry = downloadEntry;
        this.handler = handler;

    }

    public void start() {
        downloadEntry.status = DownloadEntry.DownloadStatus.downloading;
        downloadEntry.totalLength = 100;
        Message msg = handler.obtainMessage();
        msg.obj = downloadEntry;
        handler.sendMessage(msg);
        for (int i = downloadEntry.currentLength; i <= downloadEntry.totalLength; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(isPaused || isCancelled) {
                downloadEntry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancel;
                msg = handler.obtainMessage();
                msg.obj = downloadEntry;
                handler.sendMessage(msg);
                return;
            }
            downloadEntry.currentLength = i;
//            DataChanger.getInstance().postStatus(downloadEntry);
            msg = handler.obtainMessage();
            msg.obj = downloadEntry;
            handler.sendMessage(msg);
        }
        downloadEntry.status = DownloadEntry.DownloadStatus.completed;
        msg = handler.obtainMessage();
        msg.obj = downloadEntry;
        handler.sendMessage(msg);
//        DataChanger.getInstance().postStatus(downloadEntry);

    }

    public void pause() {
        isPaused = true;
    }

    public void cancel() {
        isCancelled = true;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        start();
    }
}
