package com.honaf.downloader;

import android.content.Context;

import com.honaf.downloader.db.DBController;

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
    private Context context;
    private DataChanger(Context context) {
        this.context = context;
        downloadEntries = new LinkedHashMap<>();
    }
    public synchronized static DataChanger getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DataChanger(context);
        }
        return mInstance;
    }
    public void postStatus(DownloadEntry downloadEntry) {
        DBController.getInstance(context).newOrUpdate(downloadEntry);
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

    public DownloadEntry getDownloadEntryById(String id) {
        return downloadEntries.get(id);
    }

    public void addDBDataToDownloadEntrys(String key,DownloadEntry downloadEntry){
        downloadEntries.put(key,downloadEntry);
    }

    public DownloadEntry queryDownloadEntryById(String id) {
        return downloadEntries.get(id);
    }

    public boolean containsDownloadEntryById(String id) {
        return downloadEntries.containsKey(id);
    }
}
