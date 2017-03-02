package com.honaf.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.honaf.downloader.DownloadEntry;
import com.honaf.downloader.LogUtil;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Stay
 * @version create time：Sep 5, 2014 11:37:46 AM
 */
public class DBController {
    private static DBController instance;
    private SQLiteDatabase mDB;
    private OrmDBHelper mDBhelper;

    private DBController(Context context) {
        mDBhelper = new OrmDBHelper(context);
        mDB = mDBhelper.getWritableDatabase();
    }

    public static DBController getInstance(Context context) {
        if (instance == null) {
            instance = new DBController(context);
        }
        return instance;
    }

    public synchronized void newOrUpdate(DownloadEntry entry) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            dao.createOrUpdate(entry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<DownloadEntry> queryAll() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBhelper.getDao(DownloadEntry.class);
            return (ArrayList<DownloadEntry>) dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            LogUtil.e(e.getMessage());
            return null;
        }
    }

    public synchronized DownloadEntry queryById(String id) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            LogUtil.e(e.getMessage());
            return null;
        }
    }

    public void deleteById(String id) {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBhelper.getDao(DownloadEntry.class);
            dao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
