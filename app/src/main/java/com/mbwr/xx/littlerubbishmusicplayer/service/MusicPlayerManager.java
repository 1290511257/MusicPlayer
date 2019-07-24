package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.activity.PlayActivity;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerManager extends Service {

    private MusicApp musicApp;
    private static PhoneStateListener phoneStateListener;
    private static MusicPlayerManager musicPlayerManager;
    private static MediaPlayer mediaPlayer;

    //是否暂停
    private boolean isPause = false;
    private int status = 1;//播放顺序: 1顺序循环 2随机循环 3单曲循环

    private int currentSong = -1;
    private int lastPosition = -1;

    private Album album;//歌单
    private List<Song> songList;//歌曲播放列表
    private String path;//当前歌曲路径

    private Timer timer;
    private TimerTask task;

    private int msg;

    private String TAG = MusicPlayerManager.class.getSimpleName();

    public static final String UPDATE_ACTION = "com.mbwr.xx.littlerubbishmusicplayer.service.UPDATE_ACTION";  //更新动作

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
        path = getPath();
//        songList = album.getSongs();
        musicApp = (MusicApp) this.getApplication();

        //设定音乐播放完成监听事件
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand运行.");
        currentSong = intent.getIntExtra("position", -1);//播放位置
        msg = intent.getIntExtra("MSG", -1);
        lastPosition = currentSong - 1;
        if (lastPosition < 0) {
            lastPosition = songList.size() - 1;
        }

        switch (msg) {
            case 0:
                play();
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
        stop();
        super.onDestroy();
    }

    //播放音乐
    private void play() {
        mediaPlayer.start();
        //使用Timer 定时器去定时获取当前进度
        //当歌曲暂停的时候,该task也会一直执行,待优化
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                int currentPosition = mediaPlayer.getCurrentPosition();
                Message msg = Message.obtain();
                Bundle bundle = new Bundle(); //map
                bundle.putInt("currentPosition", currentPosition);
                msg.setData(bundle);
                //发送一条消息  PlayActivity里面的handlemessage方法就会执行
                PlayActivity.handler.sendMessage(msg);
            }
        };
        //100 毫秒后 每隔1秒执行一次run方法
        timer.schedule(task, 100, 1000);
    }

    private void pause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPause = true;
            }
        }//
    }

    private void stop() {
        if (mediaPlayer != null) {
            isPause = false;
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void resume() {
        if (isPause) {
            play();
            isPause = false;
        }
    }

    private void lastMusic() {
        if (currentSong > -1) {
            path = getPath(); // 歌曲路径
        }
    }

    //下一首歌曲
    private void nextMusic() {
        int length = songList.size();
        switch (status) {
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
            case 3://单曲循环
                break;
        }
        path = getPath();

        Intent intent = new Intent(UPDATE_ACTION);
        intent.putExtra("from", 0);
        intent.putExtra("curt", currentSong);//发送当前播放歌曲
        sendBroadcast(intent);

        InitPlayMusic();
        play();
    }

    //更新播放歌单
    private void updateAlbum() {

    }

    //接受广播消息
    public class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", -1);
            //Toast.makeText(getApplicationContext(),""+control,Toast.LENGTH_SHORT).show();
            switch (control) {
                case 1:
                    status = 1;
                    break;
                case 2:
                    status = 2;
                    break;
                case 3:
                    status = 3;
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
        public void UpdateSeekBar(int position) {
        }

        //更新歌单
        @Override
        public void UpdateAlbum(Album t_album) {
            album = t_album;
            songList = album.getSongs();


        }

        @Override
        public void UpdatePlayMode(int i) {
            status = i;
        }
    }

    //音乐信息初始化
    private void InitPlayMusic() {

        Song song = songList.get(currentSong);

        mediaPlayer.reset();//恢复初始化
        try {
            mediaPlayer.setDataSource(song.getFilePath());
            mediaPlayer.prepare();//缓冲音乐
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
        mediaPlayer.getTimestamp();
        int duration = mediaPlayer.getDuration();

        Message msg = Message.obtain();
        Bundle bundle = new Bundle(); //map
        bundle.putString("songName", song.getName());
        bundle.putString("singer", song.getSinger());
        bundle.putInt("duration", duration);
        msg.setData(bundle);
        PlayActivity.handler.sendMessage(msg);

    }

    private String getPath() {
        if (album != null && currentSong != (-1)) {
            return songList.get(currentSong).getFilePath();
        }
        return null;
    }
}
