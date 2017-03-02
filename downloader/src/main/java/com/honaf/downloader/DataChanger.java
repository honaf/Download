package com.honaf.downloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by honaf on 2016/10/26.
 */

public class DataChanger extends Observable{
    private static DataChanger mInstance;
    private LinkedHashMap<String,DownloadEntry> downloadEntries;
    public DataChanger() {
        downloadEntries = new LinkedHashMap<>();
    }
    public synchronized static DataChanger getInstance() {
        if(mInstance == null) {
            mInstance = new DataChanger();
        }
        return mInstance;
    }
    public void postStatus(DownloadEntry downloadEntry) {
        downloadEntries.put(downloadEntry.id,downloadEntry);
        setChanged();
        notifyObservers(downloadEntry);
    }

    public ArrayList<DownloadEntry> queryAllPauseDownloadEntries() {
        ArrayList<DownloadEntry> downloadEntryArrayList = new ArrayList<>();
        for (Map.Entry<String, DownloadEntry> stringDownloadEntryEntry : downloadEntries.entrySet()) {
            if(stringDownloadEntryEntry.getValue().status == DownloadEntry.DownloadStatus.paused) {
                downloadEntryArrayList.add(stringDownloadEntryEntry.getValue());
            }

        }
        return downloadEntryArrayList;
    }
}
