package com.mbwr.xx.littlerubbishmusicplayer;

import android.app.Application;

import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

public class MusicApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
