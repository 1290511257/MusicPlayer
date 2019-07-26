package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.widget.Toast;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.activity.PlayActivity;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerManager extends Service {

    private MusicApp musicApp;
    private static PhoneStateListener phoneStateListener;
    private static MusicPlayerManager musicPlayerManager;
    private static MediaPlayer mediaPlayer;
    private static MusicReceiver musicReceiver;
    private static Timer timer;
    private static TimerTask task;
    private int msg;

    private Album album;//歌单
    private List<Song> songList;//歌曲播放列表
    private String path = "/storage/emulated/0/Music/蔡健雅+-+紫.mp3";//当前歌曲路径

    //是否暂停
    private boolean isPause = true;
    private int playMode = 1;//播放顺序: 1顺序循环 2随机循环 3单曲循环

    private int currentSong = -1;
    private int lastPosition = -1;

    private String TAG = MusicPlayerManager.class.getSimpleName();

    //广播
    public static final String UPDATE_MUSIC_INFO = "com.mbwr.xx.littlerubbishmusicplayer.UPDATE_MUSIC_INFO";  //更新音乐基本信息
    public static final String UPDATE_PROGRESS = "com.mbwr.xx.littlerubbishmusicplayer.UPDATE_PROGRESS";
    public static final String UPDATE_MODE_STATUS = "com.mbwr.xx.littlerubbishmusicplayer.UPDATE_MODE_STATUS";
    public static final String NEXT_MUSIC = "com.mbwr.xx.NEXT_MUSIC";
    public static final String LAST_MUSIC = "com.mbwr.xx.LAST_MUSIC";
    public static final String PLAY_MUSIC = "com.mbwr.xx.PLAY_MUSIC";


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
        mediaPlayer = new MediaPlayer();
        musicApp = (MusicApp) this.getApplication();

        //监听广播注册
        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NEXT_MUSIC);
        intentFilter.addAction(LAST_MUSIC);
        intentFilter.addAction(PLAY_MUSIC);
        registerReceiver(musicReceiver, intentFilter);


        songList = new ArrayList<>();
        Song song = new Song("歌曲名", "歌手名", album, "/storage/emulated/0/Music/蔡健雅+-+紫.mp3", "/storage/emulated/0/Music/蔡健雅+-+紫.mp3");
        songList.add(song);
        currentSong = 0;
        lastPosition = 0;

        //测试数据
        album = new Album();
        album.setName("陈奕迅专辑.");
        album.setSongs(songList);

        InitPlayMusic();
        //设定音乐播放完成监听事件
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "音乐播放完成.");
                if (playMode != 3) {//单曲循环时继续当前歌曲
                    nextMusic();
                }
            }
        });
        Log.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand运行.");
        //每次start service都会运行此方法
        updateSongInfo();
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

    //播放音乐
    private void play() {
        mediaPlayer.start();
        isPause = false;
        startTimeTask();
        Log.i(TAG, "Play StartTimeTask");
    }

    //暂停播放
    public void pause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPause = true;
                stopTimeTask();
                Log.i(TAG, "Pause StopTimeTask");
            }
        }
    }

    //停止播放
    private void stop() {
        if (mediaPlayer != null) {
            isPause = true;
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
            stopTimeTask();
        }
    }

    //继续播放
    private void resume() {
        if (isPause) {
            play();
            isPause = false;
        }
    }

    //上一首歌曲
    private void lastMusic() {
        if (currentSong > -1) {
            path = getPath(); // 歌曲路径
        }
    }

    //下一首歌曲
    private void nextMusic() {
        if (songList == null) {
            Toast.makeText(this, "没有可播放的音乐!", Toast.LENGTH_SHORT).show();
            return;
        }

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
        path = getPath();

        InitPlayMusic();
        play();


    }

    //更新播放歌单
    private void updateAlbum() {

    }

    /**
     * @author xuxiong
     * @time 7/26/19  4:07 AM
     * @describe 更新歌曲基本信息显示
     */
    private void updateSongInfo() {

        Song song = songList.get(currentSong);
        int duration = mediaPlayer.getDuration();
        int currentPosition = mediaPlayer.getCurrentPosition();

        //Hander
        Message msg = Message.obtain();
        Bundle bundle = new Bundle(); //map
        bundle.putString("songName", song.getName());
        bundle.putString("singer", song.getSinger());
        bundle.putInt("currentPosition", currentPosition);
        bundle.putInt("duration", duration);
        bundle.putBoolean("mediaStatu", isPause);
        bundle.putInt("playMode", playMode);
        msg.setData(bundle);
        PlayActivity.playHandler.sendMessage(msg);

        //BoardCast
        Intent intent = new Intent(UPDATE_MUSIC_INFO);
        Bundle bd = new Bundle();
        bd.putString("songName", song.getName());
        bd.putString("singer", song.getSinger());
        bd.putInt("currentPosition", currentPosition);
        bd.putInt("duration", duration);
        bd.putBoolean("mediaStatu", isPause);
        intent.putExtras(bd);
        sendBroadcast(intent);
    }

    private void updatePlayTime(int progress) {
        mediaPlayer.seekTo(progress);
    }
    /**
     *  @author xuxiong
     *  @time 7/26/19  4:55 AM
     *  @describe 更新界面播放模式和播放状态
     */
    private void updatePlayModeOrPlayStatus(){

    }

    /**
     * @author xuxiong
     * @time 7/26/19  4:25 AM
     * @describe 更新Progress任务
     */
    private void startTimeTask() {
        //使用Timer 定时器去定时获取当前进度
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                int currentPosition = mediaPlayer.getCurrentPosition();
                Log.i(TAG, "歌曲正在播放,播放进度:" + currentPosition);

                //Hander
                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle(); //map
                bundle.putInt("currentPosition", currentPosition);
                msg.setData(bundle);
                PlayActivity.playHandler.sendMessage(msg);

                //BoardCast
                Intent intent = new Intent(UPDATE_PROGRESS);
                Bundle bd = new Bundle();
                bd.putInt("currentPosition", currentPosition);
                intent.putExtras(bd);
                sendBroadcast(intent);
            }
        };
        //0 毫秒后 每隔1秒执行一次run方法
        timer.schedule(task, 0, 250);
    }

    //实际使用中有时不会正常停止
    private void stopTimeTask() {
        if (task != null && timer != null) {
            task.cancel();
            timer.cancel();
        }
    }

    //接受广播消息
    public class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("MusicReceiver", "onReceiver,action" + action);
            switch (action) {
                case LAST_MUSIC:
                    lastMusic();
                    break;
                case PLAY_MUSIC:
                    play();
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
            updatePlayTime(position);
        }

        //更新歌单
        @Override
        public void UpdateAlbum(Album t_album) {
            album = t_album;
            songList = album.getSongs();

        }

        @Override
        public void UpdatePlayMode(int i) {
            playMode = i;
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

    //歌曲初始化
    private void InitPlayMusic() {

        if (songList == null) {
            Toast.makeText(this, "请选择要播放的音乐!", Toast.LENGTH_SHORT).show();
            return;
        }
        Song song = songList.get(currentSong);
        mediaPlayer.reset();//恢复初始化
        try {
            mediaPlayer.setDataSource(song.getFilePath());
            mediaPlayer.prepare();//缓冲音乐
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
//        mediaPlayer.getTimestamp();
        updateSongInfo();
    }

    private String getPath() {
        if (album != null && currentSong != (-1)) {
            return songList.get(currentSong).getFilePath();
        }
        return null;
    }
}
