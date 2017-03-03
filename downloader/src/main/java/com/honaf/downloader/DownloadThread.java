package com.honaf.downloader;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by honaf on 2016/10/26.
 */

public class DownloadThread implements Runnable {
    private final String url;
    private final int index;
    private final long startPostion;
    private final long endPosition;
    private volatile String path;
    private final DownloadListener downloadListener;
    private volatile boolean isPause = false;
    private volatile DownloadEntry.DownloadStatus status;

    public DownloadThread(String url, int index, long startPostion, long endPosition, DownloadListener downloadListener) {
        this.url = url;
        this.index = index;
        this.startPostion = startPostion;
        this.endPosition = endPosition;
        this.downloadListener = downloadListener;
        this.path = Environment.getExternalStorageDirectory() + File.separator + "honaf" + File.separator + url.substring(url.lastIndexOf("/") + 1);
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
        status = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + startPostion + "-" + endPosition);
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
//            int contentLength = connection.getContentLength();
            RandomAccessFile raf;
            int length;
            byte[] bytes = new byte[1024];
            InputStream is;
            File file = new File(path);
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPostion);
                is = connection.getInputStream();
                while ((length = is.read(bytes)) != -1) {
                    if (isPause) {
                        break;
                    }
                    raf.write(bytes, 0, length);
                    downloadListener.onProgressChanged(index, length);
                }
                raf.close();
                is.close();
            } else {
                downloadListener.onDownloadError(responseCode + "");
            }
            if (isPause) {
                status = DownloadEntry.DownloadStatus.paused;
                downloadListener.onDownloadPause(index);
            } else {
                status = DownloadEntry.DownloadStatus.completed;
                downloadListener.onDownloadComplete(index);
            }

        } catch (IOException e) {
            if (isPause) {
                status = DownloadEntry.DownloadStatus.paused;
                downloadListener.onDownloadPause(index);
            } else {
                status = DownloadEntry.DownloadStatus.error;
                downloadListener.onDownloadError(e.getMessage());
            }

            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isPause() {
        return status == DownloadEntry.DownloadStatus.paused || status == DownloadEntry.DownloadStatus.completed;
    }

    public boolean isRunning() {
        return status == DownloadEntry.DownloadStatus.downloading;
    }

    public void pause() {
        isPause = true;
        Thread.currentThread().interrupt();
    }

    interface DownloadListener {
        void onProgressChanged(int index, int progress);

        void onDownloadComplete(int index);

        void onDownloadError(String message);

        void onDownloadPause(int index);
    }
}
