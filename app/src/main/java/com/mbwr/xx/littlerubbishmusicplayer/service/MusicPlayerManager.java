package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.activity.MusicPlayActivity;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;
import com.mbwr.xx.littlerubbishmusicplayer.widget.MusicWidgetProvider;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerManager extends Service {

    private static final String TAG = "--------------->" + MusicPlayerManager.class.getSimpleName();

    //广播
    public static final String UPDATE_MUSIC_INFO = "com.mbwr.xx.com.mbwr.xx.littlerubbishmusicplayer.UPDATE_MUSIC_INFO";  //更新音乐基本信息
    public static final String UPDATE_PROGRESS = "com.mbwr.xx.com.mbwr.xx.littlerubbishmusicplayer.UPDATE_PROGRESS";
    public static final String UPDATE_MODE_STATUS = "com.mbwr.xx.com.mbwr.xx.littlerubbishmusicplayer.UPDATE_MODE_STATUS";
    public static final String RESET_WIDGET = "com.mbwr.xx.com.mbwr.xx.littlerubbishmusicplayer.RESET_WIDGET";

    public static final String PLAY_MODE = MusicWidgetProvider.PLAY_MODE;
    public static final String NEXT_MUSIC = MusicWidgetProvider.NEXT_MUSIC;
    public static final String LAST_MUSIC = MusicWidgetProvider.LAST_MUSIC;
    public static final String PLAY_MUSIC = MusicWidgetProvider.PLAY_MUSIC;

    private MusicApp musicApp;
    private static PhoneStateListener mPhoneStateListener;
    private static MusicPlayerManager musicPlayerManager;
    private static MediaPlayer mediaPlayer;
    private static MusicReceiver musicReceiver;
    private static Timer timer;
    private static TimerTask task;

    private static Album mAlbum;//歌单
    public static List<Song> songList;//歌曲播放列表

    //是否正在播放
    private static boolean isPlaying = false;
    private static int playMode = 1;//播放顺序: 1顺序循环 2随机循环 3单曲循环

    public static int currentSong = -1;
    private int lastPosition = -1;

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
//        resetMusicWidget();

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

        //设定音乐播放完成监听事件
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "音乐播放完成.");
                nextMusic();
            }
        });

        //音乐准备完成事件
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "音乐播放准备完成...");
                updateSongInfo();
            }
        });

        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "OnInfoListener");
                return false;
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.i(TAG, "setOnBufferingUpdateListener");
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            //在media初始化后但并未设置资源时调用某些方法可能会触发此异常异监听
            //有些异常可能会触发音乐播放完成事件,设置ErrorListener后可以阻塞相应异常事件触发,如:OnCompletionListener
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "OnErrorListener:" + what + "," + extra);
                return true;
            }
        });

        if (InitMusicInfo()) {
            mediaPlayer.reset();//恢复初始化
            try {
                if (songList.size() > 0) {
                    mediaPlayer.setDataSource(songList.get(currentSong).getFilePath());//  song.getFilePath());
                    mediaPlayer.prepare();//缓冲音乐
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            isPlaying = mediaPlayer.isPlaying();
        } else {//缺少播放歌曲信息,启动歌单列表界面
            Log.i(TAG, "缺少播放歌曲信息,启动歌单列表界面");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand运行.");//

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        resetMusicWidget();
        stop();
        if (musicReceiver != null) {
            unregisterReceiver(musicReceiver);
        }
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:11 PM
     * @describe 开始播放歌曲
     */
    private static void play() {
        if (!mediaPlayer.isPlaying()) {
            Log.i(TAG, "play start");
            mediaPlayer.start();
            isPlaying = mediaPlayer.isPlaying();
            try {
                if (MusicApp.iMusicAidlInterface != null) {
                    MusicApp.iMusicAidlInterface.setPlayInfo(mAlbum.getId(), songList.get(currentSong).getId(), playMode);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            startTimeTask();
//            updateSongInfo();
            updatePlayModeOrPlayStatus();
        }
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:11 PM
     * @describe 暂停播放当前歌曲
     */
    public static void pause() {
        Log.i(TAG, "pause");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = mediaPlayer.isPlaying();
                stopTimeTask();
//                updateSongInfo();
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
            isPlaying = false;
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

    public void OnPause() {
        pause();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:03 PM
     * @describe 播放上一首歌曲
     */
    private void lastMusic() {
        nextMusic();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:04 PM
     * @describe 播放下一首歌曲
     */
    public void nextMusic() {
        if (!checkInitData()) return;
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
                if (currentSong == -1 && songList.size() != 0) currentSong = 0;
                break;
        }
        stopTimeTask();
        resetMediaPlayer();
        play();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:04 PM
     * @describe 重新播放当前歌曲
     */
    private void currentMusic() {
        stopTimeTask();
        resetMediaPlayer();
        play();
    }

    /**
     * @author xuxiong
     * @time 7/28/19  9:08 PM
     * @describe 更新当前播放歌单和播放歌曲
     */
    private void updatePlayAlbum(int mAlbumPosition, int mSongPosition) {
        if ((musicApp.getLocalAlbum().size() - mAlbumPosition) > 0) {
            mAlbum = musicApp.getLocalAlbum().get(mAlbumPosition);
            currentSong = mSongPosition;
        } else {

        }
    }

    /**
     * @author xuxiong
     * @time 8/5/19  9:21 PM
     * @describe 更改循环模式
     */
    private void playModeChange() {
        playMode = (playMode % 3) + 1;
        updatePlayModeOrPlayStatus();
    }

    /**
     * @author xuxiong
     * @time 7/26/19  4:07 AM
     * @describe 更新显示界面歌曲基本信息
     */
    private static void updateSongInfo() {

        if (currentSong == -1 || songList == null || songList.size() <= currentSong) return;
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
        intent.setComponent(new ComponentName(Utils.getContext(), MusicWidgetProvider.class));
        Bundle bd = new Bundle();
        bd.putString("songName", song.getName());
        bd.putString("singer", song.getSinger());
        bd.putInt("currentPosition", currentPosition);
        bd.putInt("duration", duration);
        intent.putExtras(bd);
        Utils.getContext().sendBroadcast(intent);

        updatePlayModeOrPlayStatus();

        Log.i(TAG, "updateSongInfo:\n" +
                "songName = " + song.getName() +
                "\nsinger = " + song.getSinger());
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
        intent.setComponent(new ComponentName(Utils.getContext(), MusicWidgetProvider.class));
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
                intent.setComponent(new ComponentName(Utils.getContext(), MusicWidgetProvider.class));
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
                    playModeChange();
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
            if (!checkInitData()) return;
            pause();
        }

        //按列表播放音乐
        @Override
        public void CallPlay() {
            if (!checkInitData()) return;
            play();
        }

        @Override
        public void CallPlay(int songPosition) {

        }

        //切歌单播放音乐
        @Override
        public void CallPlay(int albumId, int position) {
            if (mAlbum == null || ((albumId != -1) && (albumId != mAlbum.getId()))) {
                mAlbum = LitePal.find(Album.class, albumId);
                songList = mAlbum.getSongs();
            }
            currentSong = position;
            resetMediaPlayer();
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
        public void UpdatePlayMode() {
            playModeChange();
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

        @Override
        public boolean RemoveSong(int position) {
            return removeSong(position);
        }
    }

    /**
     * @author xuxiong
     * @time 8/7/19  7:19 AM
     * @describe 将歌曲从当前歌单中删除
     */
    private boolean removeSong(int position) {
        if (LitePal.delete(Song.class, songList.get(position).getId()) > 0) {
            songList.remove(position);
            mAlbum.setSongs(songList);
            mAlbum.save();
            return true;
        }
        return false;
    }


    //初始化设置小部件
    private void resetMusicWidget() {
        //BoardCast
        Intent intent = new Intent(MusicPlayerManager.RESET_WIDGET);
        intent.setComponent(new ComponentName(Utils.getContext(), MusicWidgetProvider.class));
        Utils.getContext().sendBroadcast(intent);
    }

    /**
     * @author xuxiong
     * @time 7/28/19  8:00 PM
     * @describe mediaPlayer初始化, 以及播放界面歌曲信息更新
     */
    private void resetMediaPlayer() {
        Log.i(TAG, "resetMediaPlayer");
        mediaPlayer.reset();//恢复初始化
        try {
            if (mAlbum != null && currentSong >= 0 && (songList.size() - currentSong) > 0) {
                mediaPlayer.setDataSource(songList.get(currentSong).getFilePath());
                mediaPlayer.prepare();//缓冲音乐
//                Log.i(TAG, songList.get(currentSong).getName());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        isPlaying = mediaPlayer.isPlaying();
//        mediaPlayer.getTimestamp();
//        updateSongInfo();
//        updatePlayModeOrPlayStatus();
    }

    /**
     * @author xuxiong
     * @time 8/4/19  10:23 PM
     * @describe 初始化设置歌曲信息
     */
    private boolean InitMusicInfo() {
        Map<String, Long> map = MusicApp.playInfo;
        if (map == null || map.size() == 0) return false;
        mAlbum = LitePal.find(Album.class, map.get("albumId"));
        if (mAlbum == null || mAlbum.getSongs() == null) return false;
        songList = mAlbum.getSongs();
        long songId = map.get("songId");
        for (Song s : songList) {
            if (s.getId() == songId) currentSong = songList.indexOf(s);
        }
        playMode = map.get("playMode").intValue();
        if (currentSong != -1 && playMode != -1 && songList != null && mAlbum != null) return true;
        return false;
    }

    private boolean checkInitData() {
        if (mAlbum == null || mAlbum.getSongs() == null || songList.size() <= currentSong)
            return false;
        return true;
    }


}
