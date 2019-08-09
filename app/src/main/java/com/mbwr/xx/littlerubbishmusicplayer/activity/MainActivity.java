package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.adapter.AlbumExpandableListAdapter;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Group;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.mbwr.xx.littlerubbishmusicplayer.service.PhoneListenerService;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mbwr.xx.littlerubbishmusicplayer.utils.Utils.getContext;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //请求储存卡权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private List<Group> gData;
    private List<List<Album>> iData;

    private ExpandableListView mAlbumListView;
    private AlbumExpandableListAdapter mAlbumExpandableListAdapter;
    private MusicApp musicApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //phoneListenerService启动
        final Intent intent = new Intent(this, PhoneListenerService.class);
        startService(intent);

        //请求储存卡权限
        verifyStoragePermissions(this);

        //打开播放界面
//        Intent intent1 = new Intent(this, MusicPlayActivity.class);
//        Intent intent1 = new Intent(this, MusicPlayActivity.class);
//        startActivity(intent1);


        musicApp = (MusicApp) getApplicationContext();
        gData = new ArrayList<>();
        gData.add(new Group("我创建的歌单"));
        gData.add(new Group("我收藏的歌单"));

        iData = new ArrayList<>();
        iData.add(musicApp.getLocalAlbum());
        iData.add(new ArrayList<Album>());

        mAlbumListView = findViewById(R.id.album_expandable_list_view);
        mAlbumExpandableListAdapter = new AlbumExpandableListAdapter(gData, iData, MainActivity.this);


        mAlbumListView.setAdapter(mAlbumExpandableListAdapter);
        mAlbumListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.i("------------>", "onChildClick");
                Long albumId = iData.get(groupPosition).get(childPosition).getId();
                Intent intent1 = new Intent(MainActivity.this, SongListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("album", albumId.intValue());
                intent1.putExtra("MainActivity", bundle);
                startActivity(intent1);

//                MainActivity.this.finish();
                return true;
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
//        showPopuWindow();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, MusicSearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, MusicPlayActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, SongListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tools) {
            showPopuWindow();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //请求储存卡权限读取
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author xuxiong
     * @time 8/9/19  4:04 AM
     * @describe 弹出编辑歌单名窗口
     */
    public void showPopuWindow() {

        final View view = LayoutInflater.from(this).inflate(R.layout.popuwindow_add_album, null);
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView mCancel, mSure;
        final EditText mAlbumEditText;
        mCancel = view.findViewById(R.id.add_album_cancel);
        mSure = view.findViewById(R.id.add_album_edited);
        mAlbumEditText = view.findViewById(R.id.add_album_editText);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        mAlbumEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("popuWindow", "run");

                mAlbumEditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) manager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }, 1000);

        mAlbumEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager manager = ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                    if (manager != null)
                        manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        //PopuWindow
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
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
        //显示控件
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
//        showSoftInputFromWindow(MainActivity.this, mAlbumEditText);
    }


    public static void showSoftInputFromWindow(Activity activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        //显示软键盘
//        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //如果上面的代码没有弹出软键盘 可以使用下面另一种方式
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }
}
