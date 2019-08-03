package com.mbwr.xx.littlerubbishmusicplayer;

import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;
import org.litepal.tablemanager.callback.DatabaseListener;

import java.util.ArrayList;
import java.util.List;

public class MusicApp extends LitePalApplication {

    private static String TAG = MusicApp.class.getName();

    private List<Song> localMusic;//所有音乐

    private List<Album> localAlbum;//所有歌单

    public void setLocalMusic(List<Song> songs) {
        this.localMusic = new ArrayList<>();
        this.localMusic = songs;
    }

    public List<Song> getLocalMusic() {
        return this.localMusic;
    }

    public void setLocalAlbum(List<Album> albums) {
        this.localAlbum = new ArrayList<>();
        this.localAlbum = albums;
    }

    public List<Album> getLocalAlbum() {
        return this.localAlbum;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化Litepal,设置数据库更新监听
        LitePal.initialize(this);
        LitePal.registerDatabaseListener(new DatabaseListener() {
            @Override
            public void onCreate() {//数据库创建时执行此方法,可用来执行一些初始化操作
                Log.i("xxxxxxxxxxxxxx", "数据库创建!!");

            }

            @Override
            public void onUpgrade(int oldVersion, int newVersion) {//表结构有更新会执行此方法
                Log.i("xxxxxxxxxxxxxx", "数据库有更新!!");
            }
        });

        if (0 == LitePal.count(Album.class)) (new Album("所有歌曲", null)).save();

//        LitePal.deleteAll(Album.class);
//        LitePal.deleteAll(Song.class);

        localAlbum = LitePal.findAll(Album.class);
        localMusic = LitePal.findAll(Song.class);

        Utils.init(this);
    }


}
