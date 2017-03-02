package com.honaf.download;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.honaf.downloader.DownloadManager;


/**
 * Created by Stay on 10/8/15.
 * Powered by www.stay4it.com
 */
public class WelcomeActivity extends AppCompatActivity {
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            jumpTo();
        }
    };

    private  void jumpTo() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DownloadManager.getInstance(getApplicationContext());
        Log.e("WelcomeActivity==>","onCreate");
        mHandler.sendEmptyMessageDelayed(0,2000);
    }
}
