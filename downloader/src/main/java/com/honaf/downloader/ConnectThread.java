package com.honaf.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by honaf on 2017/3/2.
 */

public class ConnectThread implements Runnable {
    private final String url;
    private final ConnectListener listener;
    public volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener connectListener) {
        this.url = url;
        this.listener = connectListener;
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
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                listener.onConnected(isSupportRange, contentLength);
            } else {
                listener.onConnectError("server error:" + responseCode);
            }
            isRunning = false;
        } catch (IOException e) {
            isRunning = false;
            listener.onConnectError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning(){
        return false;
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }

    interface ConnectListener {
        void onConnected(boolean isSupportRange, int totalLength);

        void onConnectError(String message);


    }
}
