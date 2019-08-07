package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.adapter.MusicSearchListAdapter;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.utils.FileUtils;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicSearchActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnTouchListener {

    private static final String TAG = MusicSearchActivity.class.getSimpleName();
    private TextView thepath, seaching, head, allchoose;
    private LinearLayout mLinearLayout;
    private ImageView mOutLocal;
    private Button mBeginSearch, mSureAdd;
    private ListView mSongList;
    private MusicSearchListAdapter musicAdapter;

    private MusicApp musicApp;
    private boolean mSelectAll = false;
    private int addSongSize = 0;

    //可新增音乐
    private List<Song> mCanAddSongList = new ArrayList<>();

    //所有已近添加过的音乐
    private static List<Song> mAddedSongList = new ArrayList<>();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                    thepath.setText((String) msg.obj);//显示搜索路径
                    break;
                case 1:
                    if (mCanAddSongList.size() == 0) {
                        head.setText("没有可以添加的歌曲");
                    }
                    head.setText("新增" + mCanAddSongList.size() + "首可添加歌曲");
                    musicAdapter.setList(mCanAddSongList);
                    musicAdapter.notifyDataSetChanged();
                    mLinearLayout.setVisibility(View.GONE);
                    allchoose.setVisibility(View.VISIBLE);
                    mSureAdd.setBackgroundResource(R.color.colorPrimary);
//                    mSureAdd.setBackgroundColor(Color.parseColor("#FF4040"));
                    mSureAdd.setEnabled(true);
                    break;
                case 2:
                    head.setText(addSongSize + "首歌曲已添加!");
                    addSongSize = 0;
                    musicAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_music_search);

        musicApp = (MusicApp) this.getApplication();
        seaching = findViewById(R.id.seaching);//
        head = findViewById(R.id.head_local);
        thepath = findViewById(R.id.what_path);
        mOutLocal = findViewById(R.id.out_local);
        mBeginSearch = findViewById(R.id.begin_seach);
        mSureAdd = findViewById(R.id.sure_list);
        mLinearLayout = findViewById(R.id.show_local);
        mSongList = findViewById(R.id.show_music);//获取list列表
        allchoose = findViewById(R.id.allchoose);
        mAddedSongList = musicApp.getLocalMusic();
        musicAdapter = new MusicSearchListAdapter(this);//list适配器
        musicAdapter.setList(mCanAddSongList);
        mSongList.setAdapter(musicAdapter);//绑定适配器
        mSongList.setOnItemClickListener(this);
        mOutLocal.setOnClickListener(this);
        mBeginSearch.setOnClickListener(this);
        mSureAdd.setOnClickListener(this);
        mSureAdd.setEnabled(false);//先设定为不可选
        allchoose.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MusicPlayActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
            case R.id.begin_seach://歌曲搜索
                mBeginSearch.setVisibility(View.GONE);
                seaching.setVisibility(View.VISIBLE);
                thepath.setVisibility(View.VISIBLE);
                new SearchMusicThread().start();
                break;
            case R.id.allchoose:
                allSelectChanges();
                break;
            case R.id.sure_list:
                progressSelectedSongs();
                mCanAddSongList = new ArrayList<>();
                break;
        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <musicApp>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicSearchListAdapter.ViewHolder vHolder = (MusicSearchListAdapter.ViewHolder) view.getTag();
        vHolder.vCheckBox.toggle();
        MusicSearchListAdapter.isSelected.put(position, vHolder.vCheckBox.isChecked());
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    /**
     * @author xuxiong
     * @time 8/1/19  9:08 PM
     * @describe 搜索歌曲线程, 用来处理具体处理逻辑
     */
    private class SearchMusicThread extends Thread {
        @Override
        public void run() {
            boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
            if (sdCardExist) {
                File sd = Environment.getExternalStorageDirectory();
                ReadMp3Files(sd.getPath());
                handler.sendEmptyMessage(1);
            } else {
                Toast.makeText(getApplicationContext(), "请插入sd卡", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @author xuxiong
     * @time 8/1/19  8:40 PM
     * @describe 获取目录下所有mp3文件, 并进行处理
     */
    public void ReadMp3Files(String rootDirPath) {

        List<String> filesPath = FileUtils.getTargetTypeFiles(rootDirPath, false, "mp3");
        if (filesPath.size() == 0) return;

        List<Album> albumList = new ArrayList<>();//默认添加歌单
        Album album = LitePal.find(Album.class, 1);
        albumList.add(album);

        for (String path : filesPath) {//单个mp3文件处理
            File mp3File = new File(path);
            long mSize = mp3File.length();
            List<String> info = getSongInfo(mp3File.getName());
            String songName = info.get(0);
            String singerName = info.get(1);
            boolean isExist = false;
            if (null == mAddedSongList) mAddedSongList = new ArrayList<>();
            for (Song song : mAddedSongList) {
                //若分解得到的名字与文件大小均相同,则默认为同一个音乐文件,跳过处理
                if (song.getName().equals(songName) && mSize == song.getSize()) {
                    isExist = true;
                    continue;
                }
            }
            if (isExist) continue;
            mCanAddSongList.add(new Song(songName, singerName, mSize, albumList, mp3File.getAbsolutePath(), null));
        }
    }

    /**
     * @author xuxiong
     * @time 8/1/19  4:22 AM
     * @describe 处理单个被选中歌曲信息:1:添加到数据库,2:加入LocalMusic
     */
    private void progressOneSelectedSong(int songPosition) {
        Song song = mCanAddSongList.get(songPosition);
        if (song.save()) {
            mAddedSongList.add(song);
//            mCanAddSongList.remove(song);
            addSongSize++;
            Log.i(TAG, "歌曲:" + song.getName() + "已添加至数据库");
        }
    }

    /**
     * @author xuxiong
     * @time 8/1/19  4:27 AM
     * @describe 添加新歌曲
     */
    private void progressSelectedSongs() {
        for (int songPosition = 0; songPosition < MusicSearchListAdapter.isSelected.size(); songPosition++) {
            if (MusicSearchListAdapter.isSelected.get(songPosition)) {//过滤非选择歌曲
                progressOneSelectedSong(songPosition);
            }
        }
        if (addSongSize == 0) {
            Toast.makeText(this, "请至少选择一首歌曲", Toast.LENGTH_SHORT).show();
        } else {
            musicApp.setLocalMusic(mAddedSongList);
            handler.sendEmptyMessage(2);
            Intent intent = new Intent();
            setResult(0, intent);
            finish();
        }
    }

    /**
     * @return list[0]:歌名,list[1]:歌手名
     * @author xuxiong
     * @time 8/2/19  3:08 AM
     * @describe 根据文件名得到歌名歌手名
     */
    private List<String> getSongInfo(String mp3FileName) {
        List<String> info = new ArrayList<>();
        String sampleFileName = mp3FileName.replace(".mp3", "").replaceAll("\\[(.*?)\\]", "").replaceAll("[\\[*\\]]", "");
        int tempIndex = sampleFileName.indexOf("-");//null = -1
        if (tempIndex == -1) {//未检测到分隔符
            info.add(sampleFileName.trim());
            info.add("未知");
            return info;
        }
        info.add(sampleFileName.substring(tempIndex + 1).trim());
        info.add(sampleFileName.substring(0, tempIndex).trim());
        return info;
    }

    /**
     * @author xuxiong
     * @time 8/3/19  2:57 AM
     * @describe 全选/取消全选
     */
    private void allSelectChanges() {
        if (mSelectAll) {
            allchoose.setText("全选");
            mSelectAll = false;
            for (int i = 0; i < mCanAddSongList.size(); i++) {
                MusicSearchListAdapter.isSelected.put(i, false);
            }
        } else {
            allchoose.setText("取消全选");
            mSelectAll = true;
            for (int i = 0; i < mCanAddSongList.size(); i++) {
                MusicSearchListAdapter.isSelected.put(i, true);
            }
        }
        musicAdapter.notifyDataSetChanged();
    }
}
