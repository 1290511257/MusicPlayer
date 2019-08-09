package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.inter.MediaController;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

public class SongListActivity extends Activity {

    private CommonAdapter<Song> mAdapter;
    private RecyclerView mRecyclerView;
    private MusicApp musicApp;

    private static final String TAG = SongListActivity.class.getSimpleName();

    private MusicServiceConnection connection;
    private MediaController mediaController;

    /**
     * Called when the activity is starting.  This is where most initialization
     * should go: calling {@link #setContentView(int)} to inflate the
     * activity's UI, using {@link #findViewById} to programmatically interact
     * with widgets in the UI, calling
     * {@link #managedQuery} to retrieve
     * cursors for data being displayed, etc.
     *
     * <p>You can call {@link #finish} from within this function, in
     * which case onDestroy() will be immediately called after {@link #onCreate} without any of the
     * rest of the activity lifecycle ({@link #onStart}, {@link #onResume}, {@link #onPause}, etc)
     * executing.
     *
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * @see #onStart
     * @see #onSaveInstanceState
     * @see #onRestoreInstanceState
     * @see #onPostCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        musicApp = (MusicApp) this.getApplication();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));//栅格布局,每行显示x个item
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<Song>(this, R.layout.recyclerview_song, musicApp.getLocalMusic()) {

            @Override
            protected void convert(ViewHolder holder, Song song, int position) {
                holder.setText(R.id.song_info_songName, song.getName());
                holder.setText(R.id.song_info_singerName, song.getSinger());
            }
        };

        //混合方式开启服务
        final Intent intent = new Intent(this, MusicPlayerManager.class);
        startService(intent);
        //调用bindservice 获取定义的中间人对象  就可以通过mediaController间接的调用服务里面的方法
        connection = new MusicServiceConnection();
        bindService(intent, connection, BIND_AUTO_CREATE);

        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                Intent intent1 = new Intent(getApplicationContext(), MusicPlayActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent1);
                mediaController.CallPlay(position);
                finish();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                mAdapter.notifyItemRemoved(position);
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
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
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     *
     * <p><em>Note: do not count on this method being called as a place for
     * saving data! For example, if an activity is editing data in a content
     * provider, those edits should be committed in either {@link #onPause} or
     * {@link #onSaveInstanceState}, not here.</em> This method is usually implemented to
     * free resources like threads that are associated with an activity, so
     * that a destroyed activity does not leave such things around while the
     * rest of its application is still running.  There are situations where
     * the system will simply kill the activity's hosting process without
     * calling this method (or any others) in it, so it should not be used to
     * do things that are intended to remain around after the process goes
     * away.
     *
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onPause
     * @see #onStop
     * @see #finish
     * @see #isFinishing
     */
    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }
}
