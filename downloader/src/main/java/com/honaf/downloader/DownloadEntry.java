package com.honaf.downloader;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by honaf on 2016/10/26.
 */
@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable {
    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String url;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public DownloadStatus status = DownloadStatus.idle;
    @DatabaseField
    public boolean enableRange;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer,Integer> progressMaps;
    public int percent;
    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public DownloadEntry() {

    }

    public void reset() {
        currentLength = 0;
        progressMaps = null;
        percent = 0;
    }

    public enum DownloadStatus{
        waiting,downloading,paused,resume,cancel, idle, connecting, completed,error
    }
    @Override
    public String toString() {
        return "DownloadEntry: "  + " is " + status.name() + " with " + currentLength + "/" + totalLength;
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
