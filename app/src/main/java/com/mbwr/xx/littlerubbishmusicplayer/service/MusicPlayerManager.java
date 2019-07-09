package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.model.Album;

import java.io.IOException;

public class MusicPlayerManager extends Service {

    private static PhoneStateListener phoneStateListener;
    private static MusicPlayerManager musicPlayerManager;
    private static MediaPlayer mediaPlayer;

    //是否暂停
    private boolean ispause = false;
    private int status = 1;//播放顺序: 1顺序循环 2随机循环 3单曲循环
    private int currentSong = -1;
    private int lastposition = -1;
    private Album album;
    private String path;


    private int msg;
    public static final String UPDATE_ACTION = "com.example.jinpeichen.musicplay.UPDATE_ACTION";  //更新动作

    public MusicPlayerManager() {
    }

    public static MusicPlayerManager getInstance(){
        try {
            if(null == musicPlayerManager){
                synchronized (MusicPlayerManager.class){
                    if (null == musicPlayerManager){
                        musicPlayerManager = new MusicPlayerManager();
                    }
                }
            }
        }catch (Exception e){
            Log.d("Exception:",e.toString());
        }
        return musicPlayerManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();

        path = album.getSongs().get(currentSong).getFilePath();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        currentSong = intent.getIntExtra("position", -1);//播放位置
        path = getPath(); // 歌曲路径
        msg = intent.getIntExtra("MSG", -1);

        lastposition = currentSong - 1;
        if(lastposition < 0){
            lastposition = album.getSongs().size() - 1;
        }
        switch (msg) {
            case 0:
                play(0);
                break;
            case 1://暂停播放
                pause();
                break;
            case 2://停止播放
                stop();
                break;
            case 3://继续播放
                resume();
                break;
            case 4:
                lastMusic();
                break;
            case 5:
                nextMusic();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    play(0);
                    break;
                case 1:resume();break;
                case 2:
                    pause();break;
            }
        }
    };

    // begin play
    private void play (int curtTime){
        try{
            mediaPlayer.reset();//恢复初始化
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();//缓冲音乐
            mediaPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //
    private void pause(){
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                ispause=true;
            }
        }//
    }

    private void stop(){
        if (mediaPlayer!=null){
            ispause=false;
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void resume(){
        if(ispause){
            mediaPlayer.start();
            ispause=false;
        }
    }

    private void lastMusic(){
        if(currentSong > -1){
            path = getPath(); // 歌曲路径
            handler.sendEmptyMessage(0);
        }
    }

    //下一首歌曲
    private void nextMusic(){
        if(currentSong > -1){
            path = getPath(); // 歌曲路径
            handler.sendEmptyMessage(0);
        }
    }

    //接受广播消息
    public class musicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", -1);
            //Toast.makeText(getApplicationContext(),""+control,Toast.LENGTH_SHORT).show();
            switch (control){
                case 1: status = 1; break;
                case 2: status = 2; break;
                case 3: status = 3; break;
            }
        }
    }

    private String getPath(){
        if(album!=null&&currentSong!=(-1)){
            album.getSongs().get(currentSong).getFilePath();
        }
        return null;
    }
}
