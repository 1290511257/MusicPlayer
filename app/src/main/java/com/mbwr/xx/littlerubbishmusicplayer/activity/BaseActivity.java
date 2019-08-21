package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mbwr.xx.littlerubbishmusicplayer.utils.LanguageContextWrapper;


import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        //Locale.getDefault().getLanguage() 获取系统本地语言
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, Locale.getDefault().getLanguage()));
        //super前调用context将无效
    }
}
