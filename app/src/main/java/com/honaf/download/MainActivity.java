package com.honaf.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.honaf.downloader.DataWatcher;
import com.honaf.downloader.DownloadEntry;
import com.honaf.downloader.DownloadManager;
import com.honaf.downloader.LogUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mDownloadBtn;
    private DownloadManager mDownloadManager;
    private DownloadEntry entry;
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            entry = data;
            if (entry.status == DownloadEntry.DownloadStatus.cancel){
                entry = null;
            }
            LogUtil.e(data.toString());
        }
    };
    private Button mDownloadPauseBtn;
    private Button mDownloadCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDownloadBtn = (Button) findViewById(R.id.mDownloadBtn);
        mDownloadBtn.setOnClickListener(this);
        mDownloadPauseBtn = (Button) findViewById(R.id.mDownloadPauseBtn);
        mDownloadPauseBtn.setOnClickListener(this);
        mDownloadCancelBtn = (Button) findViewById(R.id.mDownloadCancelBtn);
        mDownloadCancelBtn.setOnClickListener(this);
        mDownloadManager = DownloadManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(watcher);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownloadManager.removeObserver(watcher);
    }

    @Override
    public void onClick(View v) {
        if (entry == null) {
            entry = new DownloadEntry();
            entry.name = "test.jpg";
            entry.url = "http://api.stay4it.com/uploads/test.jpg";
            entry.id = "1";
        }
        switch (v.getId()) {
            case R.id.mDownloadBtn:
                mDownloadManager.add(entry);
                break;
            case R.id.mDownloadPauseBtn:
                if (entry.status == DownloadEntry.DownloadStatus.downloading) {
                    mDownloadManager.pause(entry);
                } else if (entry.status == DownloadEntry.DownloadStatus.paused) {
                    mDownloadManager.resume(entry);
                }
                break;
            case R.id.mDownloadCancelBtn:
                mDownloadManager.cancel(entry);
                break;
        }
    }
}
