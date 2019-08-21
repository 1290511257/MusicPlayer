package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.dao.DaoOperator;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.mbwr.xx.littlerubbishmusicplayer.utils.DialogTools;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumSongListActivity extends BaseActivity {

    private static final String TAG = AlbumSongListActivity.class.getSimpleName();
    private CommonAdapter<Song> mAdapter;
    private CommonAdapter<Album> mAlbumAdapter;
    private RecyclerView mRecyclerView, mAlbumRecyclerView;
    private ImageView mFinishCurrent;

    private MusicApp musicApp;
    private MusicServiceConnection connection;
    private MediaController mediaController;
    private int albumId = -1;
    private List<Song> mAlbumSongs;

    private Map<Integer, Boolean> mSelectedList = new HashMap<>();

    private boolean isOperating = false;
    private boolean isAllSelected = false;

    private ImageButton mChoiceAll;
    Button mPopCloseBottom;
    private TextView mCloseTools, mHeadText;
    //用来处理点击事件的linearlayout
    private LinearLayout mMultiplySelectBottom, mChoiceAllOutLayout, mMultiplyAdd2PlayList, mMultiplyAdd2Album, mMultiplyDelete;
    //用来控制显示隐藏的linearlayout
    private LinearLayout mLinearLayoutAbove, mLinearLayoutBelow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");

        setContentView(R.layout.activity_music_list);
        musicApp = (MusicApp) this.getApplication();

        Intent aIntent = getIntent();
        Bundle bd = aIntent.getBundleExtra("MainActivityOld");
        albumId = bd.getInt("album");
        mAlbumSongs = DaoOperator.getSongsByAlbumId(albumId);
        mHeadText = findViewById(R.id.head_local);
        mHeadText.setText(LitePal.find(Album.class, albumId).getName());

        setAllSelectFalse();

        mCloseTools = findViewById(R.id.close_mult_tools);
        mChoiceAll = findViewById(R.id.image_all_select);
        mLinearLayoutAbove = findViewById(R.id.multiply_selects);
        mLinearLayoutBelow = findViewById(R.id.songs_operator_tools);
        mMultiplySelectBottom = findViewById(R.id.multiply_select_show);
        mChoiceAllOutLayout = findViewById(R.id.image_all_select_out_layout);
        mMultiplyAdd2Album = findViewById(R.id.multiply_add_to_album);
        mMultiplyAdd2PlayList = findViewById(R.id.multiply_add_to_playlist);
        mMultiplyDelete = findViewById(R.id.multiply_delete);
        mFinishCurrent = findViewById(R.id.out_local_album);
        mRecyclerView = findViewById(R.id.song_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));//栅格布局,每行显示x个item
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new CommonAdapter<Song>(this, R.layout.recyclerview_song_list_item, mAlbumSongs) {
            @Override
            protected void convert(ViewHolder holder, Song song, int position) {
                holder.setText(R.id.song_index_text, String.valueOf(position + 1));
                holder.setText(R.id.song_info_songName, song.getName());
                holder.setText(R.id.song_info_singerName, song.getSinger());
                if (mSelectedList.get(position)) {
                    holder.setImageResource(R.id.check_image_button, R.drawable.nact_icn_choosed);
                } else {
                    holder.setImageResource(R.id.check_image_button, R.drawable.nact_icn_daily);
                }
                if (isOperating) {//多选模式
                    holder.setVisible(R.id.check_image_button, true);
                } else {
                    holder.setVisible(R.id.check_image_button, false);
                }
            }
        };
        //开启服务
        final Intent intent = new Intent(this, MusicPlayerManager.class);
        startService(intent);
        connection = new MusicServiceConnection();
        bindService(intent, connection, BIND_AUTO_CREATE);

        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {

                if (isOperating) {//多选操作下点击item事件
                    ImageButton imageButton = view.findViewById(R.id.check_image_button);
                    if (mSelectedList.get(position)) {
                        imageButton.setImageResource(R.drawable.nact_icn_daily);
                        mSelectedList.put(position, false);
                    } else {
                        imageButton.setImageResource(R.drawable.nact_icn_choosed);
                        mSelectedList.put(position, true);
                    }
                } else {
                    Intent intent1 = new Intent(getApplicationContext(), MusicPlayActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent1);
                    mediaController.CallPlay(albumId, position);
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
//                mAdapter.notifyItemRemoved(position);
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        //多选按钮开关监听事件
        mMultiplySelectBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOperating) {
                    mLinearLayoutAbove.setVisibility(View.GONE);
                    mLinearLayoutBelow.setVisibility(View.GONE);
                    isOperating = false;
                    mAdapter.notifyDataSetChanged();
                } else {
                    mLinearLayoutAbove.setVisibility(View.VISIBLE);
                    mLinearLayoutBelow.setVisibility(View.VISIBLE);
                    isOperating = true;
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        //全选事件
        mChoiceAllOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllSelected) {
                    setAllSelectFalse();
                    mAdapter.notifyDataSetChanged();
                    mChoiceAll.setImageResource(R.drawable.nact_icn_daily);
                    isAllSelected = false;
                } else {
                    setAllSelectTrue();
                    mAdapter.notifyDataSetChanged();
                    mChoiceAll.setImageResource(R.drawable.nact_icn_choosed);
                    isAllSelected = true;
                }
            }
        });

        //关闭批量操作
        mCloseTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllSelectFalse();
                mLinearLayoutAbove.setVisibility(View.GONE);
                mLinearLayoutBelow.setVisibility(View.GONE);
                isOperating = false;
                mAdapter.notifyDataSetChanged();
            }
        });

        mMultiplyAdd2PlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mMultiplyAdd2Album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAnySelected()) {
                    showSelectAlbumPopuWindow();
                } else {
                    Utils.showToastShort("请选择需要操作的数据!");
                    return;
                }
            }
        });

        mMultiplyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (albumId == 1) {
                    Utils.showToastShort("默认歌单数据不可删除!");
                    return;
                }
                if (checkAnySelected()) {
                    deleteSongs();
                } else {
                    Utils.showToastShort("请选择需要操作的数据!");
                    return;
                }
            }
        });

        mFinishCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

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

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    /**
     * @author xuxiong
     * @time 8/13/19  1:41 AM
     * @describe 全选框为True
     */
    private void setAllSelectTrue() {
        for (int i = 0; i < mAlbumSongs.size(); i++) {
            mSelectedList.put(i, true);
        }
    }

    /**
     * @author xuxiong
     * @time 8/13/19  1:41 AM
     * @describe 全选框为False
     */
    private void setAllSelectFalse() {
        for (int i = 0; i < mAlbumSongs.size(); i++) {
            mSelectedList.put(i, false);
        }
    }

    /**
     * @author xuxiong
     * @time 8/13/19  4:13 AM
     * @describe 检查是否有item被选择
     */
    private Boolean checkAnySelected() {
        if (mSelectedList.containsValue(true)) return true;
        return false;
    }

    /**
     * @author xuxiong
     * @time 8/13/19  5:04 AM
     * @describe 获得被选择的歌曲集合
     */
    private List<Song> getSelectedSongs() {
        List<Song> songList = new ArrayList<>();
        for (int i : mSelectedList.keySet()) {
            if (mSelectedList.get(i)) songList.add(mAlbumSongs.get(i));
        }
        return songList;
    }

    /**
     * @author xuxiong
     * @time 8/13/19  4:19 AM
     * @describe 删除当前歌单中的歌单
     */
    private void deleteSongs() {
        final List<Song> selectedSongs = getSelectedSongs();
        DialogTools.createConfirmDialog(AlbumSongListActivity.this, null, "是否将" + selectedSongs.size() + "首歌曲从歌单中移除",
                android.R.string.ok, android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    //确定事件
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlbumSongs.removeAll(selectedSongs);
                        Album album = LitePal.find(Album.class, albumId);
                        album.setSongs(mAlbumSongs);
                        if (album.save()) {
                            Utils.showToastShort("歌曲删除成功!");
                            setAllSelectFalse();
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAlbumSongs.addAll(selectedSongs);
                            Utils.showToastShort("歌曲删除失败!");
                        }
                    }
                }, new DialogInterface.OnClickListener() {//取消事件
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, android.R.drawable.ic_delete).show();
    }

    /**
     * @author xuxiong
     * @time 8/13/19  6:13 AM
     * @describe 添加歌曲忽略重复歌曲
     */
    private List<Song> addIgnoreDuplicateSong(List<Song> current, List<Song> addSongList) {
        for (Song addSong : addSongList) {
            String songName = addSong.getName();
            long length = addSong.getSize();
            boolean b = true;
            for (Song albumSong : current) {
                if (songName.equals(albumSong.getName()) && length == albumSong.getSize()) {
                    b = false;
                    break;
                }
            }
            if (b) current.add(addSong);
        }
        return current;
    }

    /**
     * @author xuxiong
     * @time 8/13/19  6:01 AM
     * @describe 弹出歌单选择窗口
     */
    public void showSelectAlbumPopuWindow() {

        View view = LayoutInflater.from(this).inflate(R.layout.popuwindow_album_select, null);
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopCloseBottom = view.findViewById(R.id.pop_album_list_close);
        mAlbumRecyclerView = view.findViewById(R.id.pop_recycler_view_album);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(AlbumSongListActivity.this));
//        m.setLayoutManager(new GridLayoutManager(this, 3));//栅格布局,每行显示3个item
        mAlbumRecyclerView.addItemDecoration(new DividerItemDecoration(AlbumSongListActivity.this, DividerItemDecoration.VERTICAL));

        final List<Song> selectedSongs = getSelectedSongs();

        final List<Album> albumList = new ArrayList<>();
        for (Album album :
                musicApp.getLocalAlbum()) {
            if (album.getId() != albumId) albumList.add(album);
        }

        mAlbumAdapter = new CommonAdapter<Album>(this, R.layout.recyclerview_album_list_item, albumList) {
            @Override
            protected void convert(ViewHolder holder, Album album, int position) {
                holder.setText(R.id.album_index, String.valueOf(position + 1));
                holder.setText(R.id.album_name, album.getName());
            }
        };
        mAlbumAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, final int position) {
                final Album album = albumList.get(position);
                DialogTools.createConfirmDialog(AlbumSongListActivity.this, null, "是否将" + selectedSongs.size() + "首歌曲添加到歌单:" + album.getName(),
                        android.R.string.ok, android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            //确定事件
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<Song> newList = addIgnoreDuplicateSong(album.getSongs(), selectedSongs);
                                album.setSongs(newList);
                                album.save();
                                setAllSelectFalse();
                                mAdapter.notifyDataSetChanged();
                                popupWindow.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {//取消事件
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }, android.R.drawable.ic_dialog_alert).show();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mAlbumRecyclerView.setAdapter(mAlbumAdapter);

        //PopuWindow
        popupWindow.setContentView(view);
        popupWindow.setAnimationStyle(R.style.PopuWindow_stale);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = 0.8f;//设置背景透明度
        getWindow().setAttributes(layoutParams);
        //设置点击外部消失
        popupWindow.setOutsideTouchable(false);
        //窗口消失事件
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams layoutParams1 = getWindow().getAttributes();
                layoutParams1.alpha = 1f;
                getWindow().setAttributes(layoutParams1);
            }
        });
        mPopCloseBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        //显示控件
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }
}
