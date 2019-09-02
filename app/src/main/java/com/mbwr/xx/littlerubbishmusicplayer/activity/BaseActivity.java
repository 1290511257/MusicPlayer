package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mbwr.xx.littlerubbishmusicplayer.bluetooth.MyLog;
import com.mbwr.xx.littlerubbishmusicplayer.bluetooth.LogWrapper;
import com.mbwr.xx.littlerubbishmusicplayer.utils.LanguageContextWrapper;


import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        initializeLogging();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
//        android.provider.Settings.Global.putInt();
        //Locale.getDefault().getLanguage() 获取系统本地语言
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, Locale.getDefault().getLanguage()));
        //super前调用context将无效
    }

    /**
     * Set up targets to receive log data
     */
    public void initializeLogging() {
        // Using MyLog, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        MyLog.setLogNode(logWrapper);
        MyLog.i(TAG, "Ready");
    }

}
