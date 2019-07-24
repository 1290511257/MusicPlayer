package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.mbwr.xx.littlerubbishmusicplayer.service.PhoneListenerService;

import java.lang.ref.WeakReference;


public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackAlbum, mPlayingmode, mControl, mNext, mPre, mPlaylist, mCmt, mFav, mDown, mMore, mNeedle, mOutLocal;
    private TextView mTimePlayed, mDuration;
    private SeekBar mProgress;

    private AnimatorSet mAnimatorSet;
    private BitmapFactory.Options mNewOpts;

    private View mActiveView;
    private WeakReference<ObjectAnimator> animatorWeakReference;
    private WeakReference<View> mViewWeakReference = new WeakReference<View>(null);
    private boolean isFav = false;
    private boolean isNextOrPreSetPage = false; //判断viewpager由手动滑动 还是setcruuentitem换页
    private Toolbar toolbar;
    private FrameLayout mAlbumLayout;
    private RelativeLayout mLrcViewContainer;
    private TextView mTryGetLrc;
    private LinearLayout mMusicTool;
    private SeekBar mVolumeSeek;
    private Handler mHandler;
    private Handler mPlayHandler;
    private static final int VIEWPAGER_SCROLL_TIME = 390;
    private static final int TIME_DELAY = 500;
    private static final int NEXT_MUSIC = 0;
    private static final int PRE_MUSIC = 1;
    private int playMode = 0;
    private boolean isPlaying = false;

    private Bitmap mBitmap;
    private long lastAlbum;
    private boolean print = true;
    private String TAG = PlayActivity.class.getSimpleName();

    private MusicServiceConnection connection;
    private MediaController mediaController;

    //ui线程Hander
    public static Handler handler = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        //除去action bar

        toolbar = findViewById(R.id.toolbar);

        mAlbumLayout = findViewById(R.id.headerView);
        mLrcViewContainer = findViewById(R.id.lrcviewContainer);
        mTryGetLrc = findViewById(R.id.tragetlrc);
        mMusicTool = findViewById(R.id.music_tool);

//        mBackAlbum = findViewById(R.id.albumArt);
//        mMore = findViewById(R.id.playing_more);
//        mCmt = findViewById(R.id.playing_cmt);
//        mFav = findViewById(R.id.playing_fav);

        //返回
        mOutLocal = findViewById(R.id.out_local);
        //播放模式
        mPlayingmode = findViewById(R.id.playing_mode);
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
        mDuration = findViewById(R.id.music_duration);
        //歌曲播放进度条
        mProgress = findViewById(R.id.play_seek);
        mProgress.setMax(1000);
        //注册监听事件
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "onProgressChanged");
                Log.i(TAG, progress + "");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "onStopTrackingTouch");
            }
        });
        //
        mNeedle = findViewById(R.id.needle);
        //音量seek
        mVolumeSeek = findViewById(R.id.volume_seek);

        //混合方式开启服务
        Intent intent = new Intent(this, PhoneListenerService.class);
        startService(intent);
        //调用bindservice 获取定义的中间人对象  就可以通过mediaController间接的调用服务里面的方法
        connection = new MusicServiceConnection();
        bindService(intent, connection, BIND_AUTO_CREATE);

        //注册点击事件
        mOutLocal.setOnClickListener(this);
        mPlayingmode.setOnClickListener(this);
        mControl.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPre.setOnClickListener(this);
        mPlaylist.setOnClickListener(this);
        mDown.setOnClickListener(this);


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
                break;
            case R.id.playing_mode:
                playMode++;
                playMode = playMode > 2 ? 0 : playMode;
                mediaController.UpdatePlayMode(playMode + 1);
                switch (playMode) {
                    case 0:
                        mPlayingmode.setImageResource(R.drawable.play_icn_loop);
                        Toast.makeText(this, "循环播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        mPlayingmode.setImageResource(R.drawable.play_icn_shuffle);
                        Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        mPlayingmode.setImageResource(R.drawable.play_icn_one);
                        Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case R.id.playing_play:
                if (isPlaying) {
                    mediaController.CallPause();
                    isPlaying = false;
                } else {
                    mediaController.CallPlay();
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaController = null;
        }

    }

}