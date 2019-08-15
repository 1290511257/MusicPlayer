package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.mbwr.xx.littlerubbishmusicplayer.utils.TimeUtils;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

public class MusicPlayActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "------------>" + MusicPlayActivity.class.getSimpleName();
    private static ImageView mBackAlbum, mPlayModeImage, mControl, mNext, mPre, mPlaylist, mDown, mNeedle, mOutLocal, mPopPlayModeImage, mPopRemoveAll;
    private static TextView mTimePlayed, mDuration, mSongName, mSingerName, mPopPlayModeText, mPopWindowClose, mTryGetLrc;
    private static SeekBar mProgress, mVolumeSeek;

    private Toolbar toolbar;
    private FrameLayout mAlbumLayout;
    private RelativeLayout mLrcViewContainer;
    private LinearLayout mMusicTool;

    private CommonAdapter<Song> mAdapter;
    private RecyclerView mRecyclerView;

    private static int mPlayMode = 0;
    private static boolean isPlaying = false;

    private int mOldPosition, mNewPosition;

    private MusicServiceConnection connection;
    private MediaController mediaController;
    private MusicPlayerManager musicPlayerManager;


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
                    if (mSongName == null || mSingerName == null || mProgress == null || mTimePlayed == null || mDuration == null)
                        break;
                    mSongName.setText(songName);
                    mSingerName.setText(singer);
                    mTimePlayed.setText(TimeUtils.convertIntTime2String(timePlayed));
                    mDuration.setText(TimeUtils.convertIntTime2String(songTime));
                    mProgress.setMax(songTime);
                    break;
                case 1:
                    if (mProgress == null || mTimePlayed == null) break;
                    int playedTime = msg.getData().getInt("currentPosition");
                    mProgress.setProgress(playedTime);
                    mTimePlayed.setText(TimeUtils.convertIntTime2String(playedTime));
                    break;
                case 2:
                    if (mPlayModeImage == null) break;
                    mPlayMode = msg.getData().getInt("playMode") - 1;
                    isPlaying = msg.getData().getBoolean("playStatu");
                    switch (mPlayMode) {
                        case 0:
                            mPlayModeImage.setImageResource(R.drawable.play_icn_loop);
                            break;
                        case 1:
                            mPlayModeImage.setImageResource(R.drawable.play_icn_shuffle);
                            break;
                        case 2:
                            mPlayModeImage.setImageResource(R.drawable.play_icn_one);
                            break;
                    }
                    if (isPlaying) {
                        mControl.setImageResource(R.drawable.play_rdi_btn_play);
                    } else {
                        mControl.setImageResource(R.drawable.play_rdi_btn_pause);
                    }
                    updatePupPlayModeInfo();
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
        mOutLocal = findViewById(R.id.out_local_album);
        //播放模式
        mPlayModeImage = findViewById(R.id.playing_mode);
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
        mPlayModeImage.setOnClickListener(this);
        mControl.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPre.setOnClickListener(this);
        mPlaylist.setOnClickListener(this);
        mDown.setOnClickListener(this);
        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onPostResume() {
        Log.i(TAG, "onPostResume");
        musicPlayerManager = MusicPlayerManager.getInstance();
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
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
            case R.id.out_local_album:
                finish();
                break;
            case R.id.playing_mode:
                playModeChange();
                break;
            case R.id.playing_play:
                if (isPlaying) {
                    mediaController.CallPause();
//                    mControl.setImageResource(R.drawable.play_rdi_btn_pause);
//                    isPlaying = false;
                } else {
                    mediaController.CallPlay();
//                    mControl.setImageResource(R.drawable.play_rdi_btn_play);
//                    isPlaying = true;
                }
                break;
            case R.id.playing_next:
//                mediaController.CallNextMusic();
                musicPlayerManager.nextMusic();
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
                showPopuWindow();
                break;
        }
    }

    /**
     * @author xuxiong
     * @time 8/5/19  9:13 PM
     * @describe 更改播放模式
     */
    private void playModeChange() {
        mediaController.UpdatePlayMode();
    }

    /**
     * @author xuxiong
     * @time 8/5/19  9:49 PM
     * @describe 更新播放列表界面播放模式资源
     */
    private static void updatePupPlayModeInfo() {
        if (mPopPlayModeImage == null || mPopPlayModeText == null) return;
        switch (mPlayMode) {
            case 0:
                mPopPlayModeImage.setImageResource(R.mipmap.xunhuan);
                mPopPlayModeText.setText("顺序播放");
                break;
            case 1:
                mPopPlayModeImage.setImageResource(R.mipmap.radommusic);
                mPopPlayModeText.setText("随机播放");
                break;
            case 2:
                mPopPlayModeImage.setImageResource(R.mipmap.onemusic);
                mPopPlayModeText.setText("单曲循环");
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

    /**
     * @author xuxiong
     * @time 8/5/19  1:56 AM
     * @describe 显示音乐播放列表的PopuWindow
     */
    public void showPopuWindow() {

        if (null == MusicPlayerManager.songList) {
            Utils.showToastShort("缺少可以显示的歌单!");
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.popuwindow_song_list, null);
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mRecyclerView = view.findViewById(R.id.pop_recyclerView);
        mPopPlayModeText = view.findViewById(R.id.pop_play_mode_text);
        mPopPlayModeImage = view.findViewById(R.id.pop_play_mode_change_image);
        mPopWindowClose = view.findViewById(R.id.pop_play_list_close);
        mPopRemoveAll = view.findViewById(R.id.pop_remove);
        mNewPosition = MusicPlayerManager.currentSong;

        updatePupPlayModeInfo();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MusicPlayActivity.this));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));//栅格布局,每行显示3个item
        mRecyclerView.addItemDecoration(new DividerItemDecoration(MusicPlayActivity.this, DividerItemDecoration.VERTICAL));
        mAdapter = new CommonAdapter<Song>(this, R.layout.recyclerview_song_playing_item, MusicPlayerManager.songList) {
            @Override
            protected void convert(ViewHolder holder, Song s, int position) {
                holder.setText(R.id.song_info_songName, s.getName());
                holder.setText(R.id.song_info_singerName, s.getSinger());

                if (position == mNewPosition) {//当前播放歌曲设置
                    holder.setVisible(R.id.pop_play_status_image, true);
                    holder.setImageResource(R.id.pop_play_status_image, R.drawable.song_play_icon);
                    holder.setTextColorRes(R.id.song_info_songName, R.color.colorAccent);
                    holder.setTextColorRes(R.id.song_info_singerName, R.color.colorAccent);
                    holder.setTextColorRes(R.id.dividing_line, R.color.colorAccent);
                } else {
                    holder.setVisible(R.id.pop_play_status_image, false);
                    holder.setTextColorRes(R.id.song_info_songName, R.color.colorBlack);
                    holder.setTextColorRes(R.id.song_info_singerName, R.color.colorBlack);
                    holder.setTextColorRes(R.id.dividing_line, R.color.colorBlack);
                }
            }
        };
        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                mediaController.CallPlay(-1, position);
                mOldPosition = mNewPosition;
                mNewPosition = position;
                mAdapter.notifyItemChanged(mOldPosition);
                mAdapter.notifyItemChanged(mNewPosition);
//                popupWindow.dismiss();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (mediaController.RemoveSong(position)) {
                    mAdapter.notifyItemRemoved(position);
                }
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        mPopPlayModeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.UpdatePlayMode();
            }
        });
        mPopPlayModeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.UpdatePlayMode();
            }
        });
        mPopWindowClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        //移除所有歌曲
        mPopRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(this,)
            }
        });

        //PopuWindow
        popupWindow.setContentView(view);
        popupWindow.setAnimationStyle(R.style.PopuWindow_stale);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.8f;//设置背景透明度
        getWindow().setAttributes(layoutParams);
        //设置点击外部消失
        popupWindow.setOutsideTouchable(true);
        //窗口消失事件
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams layoutParams1 = getWindow().getAttributes();
                layoutParams1.alpha = 1f;
                getWindow().setAttributes(layoutParams1);
            }
        });
        //显示控件
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }
}