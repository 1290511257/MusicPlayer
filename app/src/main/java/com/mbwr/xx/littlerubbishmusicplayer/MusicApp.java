package com.mbwr.xx.littlerubbishmusicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;
import com.mbwr.xx.littlerubbishmusicplayer.widget.MusicWidgetProvider;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;
import org.litepal.tablemanager.callback.DatabaseListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicApp extends LitePalApplication {

    private static String TAG = MusicApp.class.getName();

    private List<Song> localMusic;//所有音乐
    private List<Album> localAlbum;//所有歌单
    public static Map<String, Long> playInfo;

    public static IMusicAidlInterface iMusicAidlInterface;

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
        Log.i(TAG, "onCreate");
        //初始化Litepal,设置数据库更新监听
        LitePal.initialize(this);
        LitePal.registerDatabaseListener(new DatabaseListener() {
            @Override
            public void onCreate() {//数据库创建时执行此方法,可用来执行一些初始化操作
                Log.i(TAG, "数据库创建!!");
            }

            @Override
            public void onUpgrade(int oldVersion, int newVersion) {//表结构有更新会执行此方法
                Log.i(TAG, "数据库有更新!!");
            }
        });
        if (0 == LitePal.count(Album.class)) (new Album("所有歌曲", null)).save();

        localAlbum = LitePal.findAll(Album.class);
        localMusic = LitePal.findAll(Song.class);
        getPlayInfo();
        Utils.init(this);
        resetMusicWidget();
    }

    /**
     * @author xuxiong
     * @time 8/6/19  11:06 PM
     * @describe 获取上次播放歌曲信息
     */
    private void getPlayInfo() {
        Intent intent2 = new Intent();
        intent2.setAction("com.mbwr.xx.myapplication.ProviderInfoService.RemoteBinder");
        intent2.setPackage("com.mbwr.xx.myapplication");
        bindService(intent2, new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i("AIDL", "------------->connected");
                iMusicAidlInterface = IMusicAidlInterface.Stub.asInterface(service);
                try {
                    long albumId = iMusicAidlInterface.getAlbumId();
                    long songId = iMusicAidlInterface.getSongId();
                    long playMode = iMusicAidlInterface.getPlayMode();
                    if (albumId != -1 && songId != -1 && playMode != -1) {
                        playInfo = new HashMap<>();
                        playInfo.put("albumId", albumId);
                        playInfo.put("songId", songId);
                        playInfo.put("playMode", playMode);
                    }
//                    MyLog.i("AIDL", "拿到服务端数据:\n" +
//                            "albumId = " + albumId +
//                            "\nsongId = " + songId +
//                            "\nplayMode = " + playMode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);

    }

    //初始化设置小部件
    private void resetMusicWidget() {
        //BoardCast
        Intent intent = new Intent(MusicPlayerManager.RESET_WIDGET);
        intent.setComponent(new ComponentName(Utils.getContext(), MusicWidgetProvider.class));
        Utils.getContext().sendBroadcast(intent);
    }
}
