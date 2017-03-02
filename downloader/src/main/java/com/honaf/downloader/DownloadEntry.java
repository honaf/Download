package com.honaf.downloader;

import java.io.Serializable;

/**
 * Created by honaf on 2016/10/26.
 */

public class DownloadEntry implements Serializable {
    public String id;
    public String name;
    public String url;
    public int totalLength;
    public int currentLength;
    public DownloadStatus status = DownloadStatus.idle;

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public DownloadEntry() {

    }

    public enum DownloadStatus{
        waiting,downloading,paused,resume,cancel, idle, completed
    }
    @Override
    public String toString() {
        return "DownloadEntry: " + url + " is " + status.name() + " with " + currentLength + "/" + totalLength;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
