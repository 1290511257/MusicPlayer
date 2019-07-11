package com.mbwr.xx.littlerubbishmusicplayer.activity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;

import java.lang.ref.WeakReference;


public class PlayingActivity extends AppCompatActivity {

    private ImageView mBackAlbum, mPlayingmode, mControl, mNext, mPre, mPlaylist, mCmt, mFav, mDown, mMore, mNeedle;
    private TextView mTimePlayed, mDuration;
    private SeekBar mProgress;

    private ActionBar ab;

    private ObjectAnimator mNeedleAnim, mRotateAnim;

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
    private Bitmap mBitmap;
    private long lastAlbum;
    private boolean print = true;
    private String TAG = PlayingActivity.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playing);


//        setContentView(R.layout.activity_playing);


//        toolbar = findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//            ab = getSupportActionBar();
//            ab.setDisplayHomeAsUpEnabled(true);
//            ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
//            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onBackPressed();
//                }
//            });
//        }
//
//
//
//        mAlbumLayout = findViewById(R.id.headerView);
//        mLrcViewContainer = findViewById(R.id.lrcviewContainer);
//        mTryGetLrc = findViewById(R.id.tragetlrc);
//        mMusicTool = findViewById(R.id.music_tool);
//
//        mBackAlbum = findViewById(R.id.albumArt);
//        mPlayingmode = findViewById(R.id.playing_mode);
//        mControl = findViewById(R.id.playing_play);
//        mNext = findViewById(R.id.playing_next);
//        mPre = findViewById(R.id.playing_pre);
//        mPlaylist = findViewById(R.id.playing_playlist);
//        mMore = findViewById(R.id.playing_more);
//        mCmt = findViewById(R.id.playing_cmt);
//        mFav = findViewById(R.id.playing_fav);
//        mDown = findViewById(R.id.playing_down);
//        mTimePlayed = findViewById(R.id.music_duration_played);
//        mDuration = findViewById(R.id.music_duration);
//
        //进度条事件监听
        mProgress = findViewById(R.id.play_seek);
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//
//        mNeedle = findViewById(R.id.needle);
//
//        mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -25, 0);
//        mNeedleAnim.setDuration(200);
//        mNeedleAnim.setInterpolator(new LinearInterpolator());
//
//        mVolumeSeek = findViewById(R.id.volume_seek);
//        mProgress.setIndeterminate(false);
//        mProgress.setProgress(1);
//        mProgress.setMax(1000);
    }
}