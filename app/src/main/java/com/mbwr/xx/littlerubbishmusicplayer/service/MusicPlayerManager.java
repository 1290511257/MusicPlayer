package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.activity.MusicPlayActivity;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerManager extends Service {

    private MusicApp musicApp;
    private static PhoneStateListener mPhoneStateListener;
    private static MusicPlayerManager musicPlayerManager;
    private static MediaPlayer mediaPlayer;
    private static MusicReceiver musicReceiver;
    private static Timer timer;
    private static TimerTask task;
    private int msg;

    private List<Album> mAlbums;
    private Album mAlbum;//歌单
    private static List<Song> songList;//歌曲播放列表
    private String path = "/storage/emulated/0/Music/蔡健雅+-+紫.mp3";//当前歌曲路径

    //是否正在播放
    private static boolean isPlaying = false;
    private static int playMode = 1;//播放顺序: 1顺序循环 2随机循环 3单曲循环

    private static int currentSong = -1;
    private int lastPosition = -1;

    private static String TAG = MusicPlayerManager.class.getSimpleName();

    //广播
    public static final String UPDATE_MUSIC_INFO = "com.mbwr.xx.littlerubbishmusicplayer.UPDATE_MUSIC_INFO";  //更新音乐基本信息
    public static final String UPDATE_PROGRESS = "com.mbwr.xx.littlerubbishmusicplayer.UPDATE_PROGRESS";
    public static final String UPDATE_MODE_STATUS = "com.mbwr.xx.littlerubbishmusicplayer.UPDATE_MODE_STATUS";

    public static final String PLAY_MODE = "com.mbwr.xx.PLAY_MODE";
    public static final String NEXT_MUSIC = "com.mbwr.xx.NEXT_MUSIC";
    public static final String LAST_MUSIC = "com.mbwr.xx.LAST_MUSIC";
    public static final String PLAY_MUSIC = "com.mbwr.xx.PLAY_MUSIC";

    public static Handler phoneListenerHander = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {// 0:电话响起,音乐暂停; 1:电话挂断,重新播放
                case 0:
                    pause();
                    break;
                case 1:
                    play();
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
    };

    public MusicPlayerManager() {

    }

    public static MusicPlayerManager getInstance() {
        try {
            if (null == musicPlayerManager) {
                synchronized (MusicPlayerManager.class) {
                    if (null == musicPlayerManager) {
                        musicPlayerManager = new MusicPlayerManager();
                    }
                }
            }
        } catch (Exception e) {
            Log.d("Exception:", e.toString());
        }
        return musicPlayerManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayBinder();
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate start");

        mediaPlayer = new MediaPlayer();
        musicApp = (MusicApp) this.getApplication();

        //监听广播注册
        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY_MODE);
        intentFilter.addAction(NEXT_MUSIC);
        intentFilter.addAction(LAST_MUSIC);
        intentFilter.addAction(PLAY_MUSIC);
        registerReceiver(musicReceiver, intentFilter);

        //测试数据
        songList = new ArrayList<>();
        Song song = new Song("歌曲名", "歌手名", 0, mAlbums, "/storage/emulated/0/Music/蔡健雅+-+紫.mp3", "/storage/emulated/0/Music/蔡健雅+-+紫.mp3");
        songList.add(song);
        currentSong = 0;
        lastPosition = 0;
        mAlbum = new Album();
        mAlbum.setName("陈奕迅专辑.");
        mAlbum.setSongs(songList);
        //end

        //
        mediaPlayer.reset();//恢复初始化
        try {
            mediaPlayer.setDataSource(song.getFilePath());
            mediaPlayer.prepare();//缓冲音乐
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
        isPlaying = mediaPlayer.isPlaying();
        //


        //设定音乐播放完成监听事件
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "音乐播放完成.");
                nextMusic();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand运行.");
        //每次start service都会运行此方法
        updateSongInfo();
        updatePlayModeOrPlayStatus();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        if (musicReceiver != null) {
            unregisterReceiver(musicReceiver);
        }
        super.onDestroy();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:03 PM
     * @describe 播放上一首歌曲
     */
    private void lastMusic() {

    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:04 PM
     * @describe 播放下一首歌曲
     */
    private void nextMusic() {

        int length = songList.size();
        switch (playMode) {
            case 1://顺序播放
                currentSong += 1;
                if (currentSong > length - 1) {
                    currentSong = 0;
                }
                break;
            case 2://随机播放
                if (length > 1) {
                    int cx = new Random().nextInt(length);
                    while (cx == currentSong) {
                        cx = new Random().nextInt(length);
                    }
                    lastPosition = currentSong;
                    currentSong = cx;
                }
                break;
            case 3:
                currentSong += 1;
                if (currentSong > length - 1) {
                    currentSong = 0;
                }
                break;
        }
        resetMediaPlayer();
        play();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:04 PM
     * @describe 重新播放当前歌曲
     */
    private void currentMusic() {
        resetMediaPlayer();
        play();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:08 PM
     * @describe 更新当前播放歌单和播放歌曲
     */
    private void updatePlayAlbum(int mAlbumPosition, int mSongPosition) {
        if ((mAlbums.size() - mAlbumPosition) > 0) {
            mAlbum = mAlbums.get(mAlbumPosition);
            currentSong = mSongPosition;
        } else {

        }
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:11 PM
     * @describe 开始播放歌曲
     */
    private static void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = mediaPlayer.isPlaying();
            startTimeTask();
            updateSongInfo();
            updatePlayModeOrPlayStatus();
        }
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:11 PM
     * @describe 暂停播放当前歌曲
     */
    public static void pause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = mediaPlayer.isPlaying();
                stopTimeTask();
                updateSongInfo();
                updatePlayModeOrPlayStatus();
            }
        }
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:12 PM
     * @describe 停止播放
     */
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
            isPlaying = mediaPlayer.isPlaying();
            stopTimeTask();
            updatePlayModeOrPlayStatus();
        }
    }

    /**
     * @author xuxiong
     * @time 8/3/19  3:32 AM
     * @describe 继续播放当前音乐
     */
    private void resume() {
        if (!mediaPlayer.isPlaying()) {
            play();
        }
    }


    /**
     * @author xuxiong
     * @time 7/26/19  4:07 AM
     * @describe 更新歌曲基本信息显示
     */
    private static void updateSongInfo() {

        Song song = songList.get(currentSong);
        int duration = mediaPlayer.getDuration();
        int currentPosition = mediaPlayer.getCurrentPosition();
        //Hander
        Message msg = Message.obtain();
        msg.what = 0;
        Bundle bundle = new Bundle(); //map
        bundle.putString("songName", song.getName());
        bundle.putString("singer", song.getSinger());
        bundle.putInt("currentPosition", currentPosition);
        bundle.putInt("duration", duration);
        msg.setData(bundle);
        MusicPlayActivity.playHandler.sendMessage(msg);

        //BoardCast
        Intent intent = new Intent(UPDATE_MUSIC_INFO);
        Bundle bd = new Bundle();
        bd.putString("songName", song.getName());
        bd.putString("singer", song.getSinger());
        bd.putInt("currentPosition", currentPosition);
        bd.putInt("duration", duration);
        intent.putExtras(bd);
        Utils.getContext().sendBroadcast(intent);
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:46 PM
     * @describe 设定播放进度
     */
    private void setPlayTime(int progress) {
        mediaPlayer.seekTo(progress);
    }

    /**
     * @author xuxiong
     * @time 7/26/19  4:55 AM
     * @describe 更新界面播放模式和播放状态
     */
    private static void updatePlayModeOrPlayStatus() {
        //Hander
        Message msg = Message.obtain();
        msg.what = 2;
        Bundle bundle = new Bundle(); //map
        bundle.putInt("playMode", playMode);
        bundle.putBoolean("playStatu", isPlaying);
        msg.setData(bundle);
        MusicPlayActivity.playHandler.sendMessage(msg);

        //BoardCast
        Intent intent = new Intent(UPDATE_MODE_STATUS);
        Bundle bd = new Bundle();
        bd.putInt("playMode", playMode);
        bd.putBoolean("playStatu", isPlaying);
        intent.putExtras(bd);
        Utils.getContext().sendBroadcast(intent);
    }

    /**
     * @author xuxiong
     * @time 7/26/19  4:25 AM
     * @describe 更新Progress任务
     */
    private static void startTimeTask() {
        //使用Timer 定时器去定时更新播放进度
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                int currentPosition = mediaPlayer.getCurrentPosition();

//                Log.i(TAG, "歌曲:" + songList.get(currentSong).getName() + "   正在播放,播放进度:" + currentPosition);
                //Hander
                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle(); //map
                bundle.putInt("currentPosition", currentPosition);
                msg.setData(bundle);
                MusicPlayActivity.playHandler.sendMessage(msg);

                //BoardCast
                Intent intent = new Intent(UPDATE_PROGRESS);
                Bundle bd = new Bundle();
                bd.putInt("currentPosition", currentPosition);
                intent.putExtras(bd);
                Utils.getContext().sendBroadcast(intent);
            }
        };
        //0 毫秒后 每隔1秒执行一次run方法
        timer.schedule(task, 0, 250);
    }

    //实际使用中有时不会正常停止   ?
    private static void stopTimeTask() {
        if (task != null && timer != null) {
            task.cancel();
            timer.cancel();
        }
    }

    /**
     * @author xuxiong
     * @time 7/29/19  3:22 AM
     * @describe 广播消息接收类
     */
    public class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("MusicReceiver", "onReceiver,action" + action);
            switch (action) {
                case PLAY_MODE:
                    playMode = (playMode % 3) + 1;
                    updatePlayModeOrPlayStatus();
                    break;
                case LAST_MUSIC:
                    lastMusic();
                    break;
                case PLAY_MUSIC:
                    if (mediaPlayer.isPlaying()) {
                        pause();
                    } else {
                        play();
                    }
                    break;
                case NEXT_MUSIC:
                    nextMusic();
                    break;
            }
        }
    }

    //定义一个binder对像用以实现服务方法调用
    private class MusicPlayBinder extends Binder implements MediaController {

        @Override
        public void CallStop() {
            stop();
        }

        @Override
        public void CallPause() {
            pause();
        }

        //按列表播放音乐
        @Override
        public void CallPlay() {
            play();
        }

        //播放当前歌单指定音乐
        @Override
        public void CallPlay(int position) {
            currentSong = position;
            play();
        }

        @Override
        public void CallResume() {
            resume();
        }

        @Override
        public void CallLastMusic() {
            lastMusic();
        }

        @Override
        public void CallNextMusic() {
            nextMusic();
        }

        @Override
        public void UpdatePlayTime(int position) {
            setPlayTime(position);
        }

        //更新歌单
        @Override
        public void UpdateAlbum(Album t_album) {
            mAlbum = t_album;
            songList = mAlbum.getSongs();

        }

        @Override
        public void UpdatePlayMode(int i) {
            playMode = i;
            updatePlayModeOrPlayStatus();
        }

        @Override
        public void StartTimeTask() {
            startTimeTask();
        }

        @Override
        public void StopTimeTask() {
            stopTimeTask();
        }

        @Override
        public void UpdateSongInfo() {
        }
    }

    /**
     * @author xuxiong
     * @time 7/28/19  8:00 PM
     * @describe mediaPlayer初始化, 以及播放界面歌曲信息更新
     */
    private void resetMediaPlayer() {
        mediaPlayer.reset();//恢复初始化
        try {
            if (mAlbum != null && currentSong >= 0 && (songList.size() - currentSong) > 0) {
                mediaPlayer.setDataSource(songList.get(currentSong).getFilePath());
            }
            mediaPlayer.prepare();//缓冲音乐
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
        isPlaying = mediaPlayer.isPlaying();
//        mediaPlayer.getTimestamp();
        updateSongInfo();
        updatePlayModeOrPlayStatus();
    }

}
