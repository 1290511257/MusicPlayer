package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.mbwr.xx.littlerubbishmusicplayer.utils.TimeUtils;

public class MusicPlayActivity extends BaseActivity implements View.OnClickListener {

    private static ImageView mBackAlbum, mPlayingMode, mControl, mNext, mPre, mPlaylist, mDown, mNeedle, mOutLocal;
    private static TextView mTimePlayed, mDuration, mSongName, mSingerName;
    private static SeekBar mProgress;

    private Toolbar toolbar;
    private FrameLayout mAlbumLayout;
    private RelativeLayout mLrcViewContainer;
    private TextView mTryGetLrc;
    private LinearLayout mMusicTool;
    private SeekBar mVolumeSeek;

    private int playMode = 0;
    private boolean isPlaying = false;

    private String TAG = MusicPlayActivity.class.getSimpleName();

    private MusicServiceConnection connection;
    private MediaController mediaController;

    //ui线程Hander,用以控制音乐播放界面
    public static Handler playHandler = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {//what==0:音乐信息初始化(歌名,长度等);what==1:播放进度更新;what==2:播放状态更新
                case 0:
                    String songName = msg.getData().getString("songName");
                    String singer = msg.getData().getString("singer");
                    int songTime = msg.getData().getInt("duration");
                    int timePlayed = msg.getData().getInt("currentPosition");
                    mSongName.setText(songName);
                    mSingerName.setText(singer);
                    mTimePlayed.setText(TimeUtils.convertIntTime2String(timePlayed));
                    mDuration.setText(TimeUtils.convertIntTime2String(songTime));
                    mProgress.setMax(songTime);
                    break;
                case 1:
                    int playedTime = msg.getData().getInt("currentPosition");
                    mProgress.setProgress(playedTime);
                    mTimePlayed.setText(TimeUtils.convertIntTime2String(playedTime));
                    break;
                case 2:
                    int playMode = msg.getData().getInt("playMode");
                    boolean isPlaying = msg.getData().getBoolean("playStatu");
                    switch (playMode - 1) {
                        case 0:
                            mPlayingMode.setImageResource(R.drawable.play_icn_loop);
                            break;
                        case 1:
                            mPlayingMode.setImageResource(R.drawable.play_icn_shuffle);
                            break;
                        case 2:
                            mPlayingMode.setImageResource(R.drawable.play_icn_one);
                            break;
                    }
                    if(isPlaying){
                        mControl.setImageResource(R.drawable.play_rdi_btn_play);
                    }else {
                        mControl.setImageResource(R.drawable.play_rdi_btn_pause);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_playing);


        toolbar = findViewById(R.id.toolbar);

        mAlbumLayout = findViewById(R.id.headerView);
        mLrcViewContainer = findViewById(R.id.lrcViewContainer);
        mTryGetLrc = findViewById(R.id.targetLrc);
        mMusicTool = findViewById(R.id.music_tool);

//        mBackAlbum = findViewById(R.id.albumArt);
//        mMore = findViewById(R.id.playing_more);
//        mCmt = findViewById(R.id.playing_cmt);
//        mFav = findViewById(R.id.playing_fav);

        //音乐时长
        mDuration = findViewById(R.id.music_duration_total);
        mTimePlayed = findViewById(R.id.music_duration_played);
        mSongName = findViewById(R.id.songName);
        mSingerName = findViewById(R.id.singerName);

        //返回
        mOutLocal = findViewById(R.id.out_local);
        //播放模式
        mPlayingMode = findViewById(R.id.playing_mode);
        //播放&暂停
        mControl = findViewById(R.id.playing_play);
        //下一曲
        mNext = findViewById(R.id.playing_next);
        //上一曲
        mPre = findViewById(R.id.playing_pre);
        //歌曲列表
        mPlaylist = findViewById(R.id.playing_playlist);
        //下载歌曲
        mDown = findViewById(R.id.playing_down);
        //播放时长
        mTimePlayed = findViewById(R.id.music_duration_played);
        //歌曲总时长
        mDuration = findViewById(R.id.music_duration_total);
        //歌曲播放进度条
        mProgress = findViewById(R.id.play_seek);
//        mProgress.setMax(1000);
        //注册监听事件
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaController.UpdatePlayTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaController.StopTimeTask();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaController.StartTimeTask();
            }
        });
        //
        mNeedle = findViewById(R.id.needle);
        //音量seek
        mVolumeSeek = findViewById(R.id.volume_seek);

        //混合方式开启服务
        Intent intent = new Intent(this, MusicPlayerManager.class);
        startService(intent);
        //调用bindservice 获取定义的中间人对象  就可以通过mediaController间接的调用服务里面的方法
        connection = new MusicServiceConnection();
        bindService(intent, connection, BIND_AUTO_CREATE);
        //注册点击事件
        mOutLocal.setOnClickListener(this);
        mPlayingMode.setOnClickListener(this);
        mControl.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPre.setOnClickListener(this);
        mPlaylist.setOnClickListener(this);
        mDown.setOnClickListener(this);
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        //解绑服务
        unbindService(connection);
        super.onDestroy();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.out_local:
                finish();
                break;
            case R.id.playing_mode:
                playMode++;
                playMode = playMode > 2 ? 0 : playMode;
                mediaController.UpdatePlayMode(playMode + 1);
                switch (playMode) {
                    case 0:
                        mPlayingMode.setImageResource(R.drawable.play_icn_loop);
                        Toast.makeText(this, "循环播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        mPlayingMode.setImageResource(R.drawable.play_icn_shuffle);
                        Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mPlayingMode.setImageResource(R.drawable.play_icn_one);
                        Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.playing_play:
                if (isPlaying) {
                    mediaController.CallPause();
                    mControl.setImageResource(R.drawable.play_rdi_btn_pause);
                    isPlaying = false;
                } else {
                    mediaController.CallPlay();
                    mControl.setImageResource(R.drawable.play_rdi_btn_play);
                    isPlaying = true;
                }
                break;
            case R.id.playing_next:
                mediaController.CallNextMusic();
                break;
            case R.id.playing_pre:
                mediaController.CallLastMusic();
                break;
            case R.id.playing_down:
                //文件下载
                mediaController.UpdateSongInfo();
                break;
            case R.id.playing_playlist:
                //显示当前歌单列表
                break;
        }
    }

    //监听服务的状态
    private class MusicServiceConnection implements ServiceConnection {

        //当服务连接成功
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取中间人对象
            mediaController = (MediaController) service;
            Log.i(TAG, "服务连接成功!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "服务断开连接.....");
            mediaController = null;
        }

    }
}