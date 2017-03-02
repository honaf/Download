package com.honaf.downloader;


import java.util.Observable;
import java.util.Observer;

/**
 * Created by honaf on 2016/10/26.
 */

public abstract class DataWatcher implements Observer {
    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param obj an argument passed to the <code>notifyObservers</code>
     */
    @Override
    public void update(Observable o, Object obj) {
       if(obj instanceof DownloadEntry) {
           notifyUpdate((DownloadEntry)obj);
       }
    }

    public abstract void notifyUpdate(DownloadEntry downloadEntry);

}
