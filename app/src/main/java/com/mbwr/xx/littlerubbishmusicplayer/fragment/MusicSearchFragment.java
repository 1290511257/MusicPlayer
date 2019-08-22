package com.mbwr.xx.littlerubbishmusicplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MusicSearchFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnTouchListener {

    private static final String TAG = MusicSearchFragment.class.getSimpleName();

    private TextView mPath, mSearching, head, mAllChoose;
    private LinearLayout mLinearLayout;
    private ImageView mOutLocal;
    private Button mBeginSearch, mSureAdd;
    private ListView mSongList;
    private MusicSearchListAdapter musicAdapter;

    private MusicApp musicApp;
    private boolean mSelectAll = false;
    private int addSongSize = 0;

    //可新增音乐
    private List<Song> mCanAddSongList;

    //所有已近添加过的音乐
    private static List<Song> mAddedSongList;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                    mPath.setText((String) msg.obj);//显示搜索路径
                    break;
                case 1:
                    if (mCanAddSongList.size() == 0) {
                        head.setText("没有可以添加的歌曲");
                    }
                    head.setText("新增" + mCanAddSongList.size() + "首可添加歌曲");
                    musicAdapter.setList(mCanAddSongList);
                    musicAdapter.notifyDataSetChanged();
                    mLinearLayout.setVisibility(View.GONE);
                    mAllChoose.setVisibility(View.VISIBLE);
                    mSureAdd.setBackgroundResource(R.color.color_orange_red);
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

    public MusicSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_local_music_search, container, false);

        mCanAddSongList = new ArrayList<>();

        musicApp = (MusicApp) MusicApp.getContext();
        mSearching = view.findViewById(R.id.seaching);//
        head = view.findViewById(R.id.head_local);
        mPath = view.findViewById(R.id.what_path);
        mOutLocal = view.findViewById(R.id.out_local_album);
        mBeginSearch = view.findViewById(R.id.begin_seach);
        mSureAdd = view.findViewById(R.id.sure_list);
        mLinearLayout = view.findViewById(R.id.show_local);
        mSongList = view.findViewById(R.id.show_music);//获取list列表
        mAllChoose = view.findViewById(R.id.all_choose);
        mAddedSongList = musicApp.getLocalMusic();
        musicAdapter = new MusicSearchListAdapter(Utils.getContext());//list适配器
        musicAdapter.setList(mCanAddSongList);
        mSongList.setAdapter(musicAdapter);//绑定适配器
        mSongList.setOnItemClickListener(this);
        mOutLocal.setOnClickListener(this);
        mBeginSearch.setOnClickListener(this);
        mSureAdd.setOnClickListener(this);
        mSureAdd.setEnabled(false);//先设定为不可选
        mAllChoose.setOnClickListener(this);

        return view;
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
//                finish();
                break;
            case R.id.begin_seach://歌曲搜索
                mBeginSearch.setVisibility(View.GONE);
                mSearching.setVisibility(View.VISIBLE);
                mPath.setVisibility(View.VISIBLE);
                new SearchMusicThread().start();
                break;
            case R.id.all_choose:
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
        MusicSearchListAdapter.mIsSelected.put(position, vHolder.vCheckBox.isChecked());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private class SearchMusicThread extends Thread {
        @Override
        public void run() {
            boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
            if (sdCardExist) {
                File sd = Environment.getExternalStorageDirectory();
                ReadMp3Files(sd.getPath());
                handler.sendEmptyMessage(1);
            } else {
                Toast.makeText(Utils.getContext(), "请插入sd卡", Toast.LENGTH_SHORT).show();
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
        for (int songPosition = 0; songPosition < MusicSearchListAdapter.mIsSelected.size(); songPosition++) {
            if (MusicSearchListAdapter.mIsSelected.get(songPosition)) {//过滤非选择歌曲
                progressOneSelectedSong(songPosition);
            }
        }
        if (addSongSize == 0) {
//            Toast.makeText(this, "请至少选择一首歌曲", Toast.LENGTH_SHORT).show();
        } else {
            musicApp.setLocalMusic(mAddedSongList);
            handler.sendEmptyMessage(2);
//            Intent intent = new Intent();
//            setResult(0, intent);
//            finish();
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
            mAllChoose.setText("全选");
            mSelectAll = false;
            for (int i = 0; i < mCanAddSongList.size(); i++) {
                MusicSearchListAdapter.mIsSelected.put(i, false);
            }
        } else {
            mAllChoose.setText("取消全选");
            mSelectAll = true;
            for (int i = 0; i < mCanAddSongList.size(); i++) {
                MusicSearchListAdapter.mIsSelected.put(i, true);
            }
        }
        musicAdapter.notifyDataSetChanged();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
}
