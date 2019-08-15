package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.MusicApp;
import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.adapter.AlbumExpandableListAdapter;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Group;
import com.mbwr.xx.littlerubbishmusicplayer.service.PhoneListenerService;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

import java.util.ArrayList;
import java.util.List;

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
    private List<Album> albums;
    private int mGroupPosition;

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

        musicApp = (MusicApp) getApplicationContext();
        gData = new ArrayList<>();
        gData.add(new Group("我创建的歌单"));
        gData.add(new Group("我收藏的歌单"));

        albums = musicApp.getLocalAlbum();

        iData = new ArrayList<>();
        iData.add(albums);
        iData.add(new ArrayList<Album>());

        mAlbumListView = findViewById(R.id.album_expandable_list_view);
        mAlbumExpandableListAdapter = new AlbumExpandableListAdapter(gData, iData, MainActivity.this) {
            @Override
            public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                ViewHolderGroup groupHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(
                            R.layout.exlist_group_item, parent, false);
                    groupHolder = new ViewHolderGroup();
                    groupHolder.tv_group_name = convertView.findViewById(R.id.tv_group_name);
                    groupHolder.tv_group_add = convertView.findViewById(R.id.add_music_album);
                    if (groupPosition == 0) {
                        groupHolder.tv_group_add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mGroupPosition = groupPosition;
                                showDialog();
//                                showPopuWindow();
                            }
                        });
                    } else {
                        groupHolder.tv_group_add.setVisibility(View.GONE);
                    }
                    convertView.setTag(groupHolder);
                } else {
                    groupHolder = (ViewHolderGroup) convertView.getTag();
                }
                groupHolder.tv_group_name.setText(gData.get(groupPosition).getgName());
                return super.getGroupView(groupPosition, isExpanded, convertView, parent);
            }
        };

        mAlbumListView.setAdapter(mAlbumExpandableListAdapter);
        mAlbumListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.i("------------>", "onChildClick");
//                selectAlbumSong = DaoOperator.getSongsByAlbumId(iData.get(groupPosition).get(childPosition).getId());
//                mAdapter.notifyDataSetChanged();
//                mAlbumsLayout.setVisibility(View.GONE);
//                mSongsLayout.setVisibility(View.VISIBLE);
                Intent intent1 = new Intent(MainActivity.this, AlbumSongListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("album", ((Long) iData.get(groupPosition).get(childPosition).getId()).intValue());
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

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

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
     * @time 8/12/19  1:16 AM
     * @describe 显示添加歌单的dialog
     */
    private void showDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("歌单标题");
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("创建新的歌单")
                .setIcon(android.R.drawable.ic_input_add)
                .setView(editText)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null).create();

//        editText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleSoftInputFromWindow();
//            }
//        });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                //确定按键
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String albumText = editText.getText().toString().trim();
                        if ("".equals(albumText)) {
                            Utils.showToastShort("请输入歌单名!!!");
                            return;
                        }
                        switch (mGroupPosition) {
                            case 0:
                                if (isAlbumExist(albumText)) {
                                    Utils.showToastShort("当前歌单已存在!!!");
                                    return;
                                } else {
                                    Album album = new Album(albumText, null);
                                    if (album.save()) {
                                        Utils.showToastShort("创建歌单成功!");
                                        albums.add(album);
                                        musicApp.setLocalAlbum(albums);
                                        hideSoftInputFromWindow(editText);
                                        alertDialog.dismiss();
                                        MyNotifyDataSetChanged();
                                    } else {
                                        Utils.showToastShort("歌单创建失败!");
                                    }
                                }
                                break;
                            case 1:
                        }
                    }
                });

                //取消按键
                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftInputFromWindow(v);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    /**
     * @author xuxiong
     * @time 8/11/19  11:01 PM
     * @describe 检验是否存在同名歌单
     */
    private Boolean isAlbumExist(String albumName) {
        MusicApp musicApp = (MusicApp) getApplication();
        for (Album album :
                musicApp.getLocalAlbum()) {
            if (album.getName().equals(albumName)) return true;
        }
        return false;
    }

    /**
     * @author xuxiong
     * @time 8/11/19  11:18 PM
     * @describe 通知视图适配器更新
     */
    private void MyNotifyDataSetChanged() {
        iData = new ArrayList<>();
        iData.add(albums);
        iData.add(new ArrayList<Album>());
        mAlbumExpandableListAdapter.notifyDataSetChanged();
    }

    /**
     * @author xuxiong
     * @time 8/9/19  4:04 AM
     * @describe 弹出编辑歌单名窗口
     */
    private void showPopuWindow() {

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
                mAlbumEditText.setFocusable(true);
                mAlbumEditText.setFocusableInTouchMode(true);
                mAlbumEditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.showSoftInput(mAlbumEditText, InputMethodManager.SHOW_FORCED);
            }
        }, 1000);

        mAlbumEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager manager = ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                    if (manager != null)
                        manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        //PopuWindow
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
//        popupWindow.showAsDropDown(getWindow().getDecorView());
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
        mAlbumEditText.requestFocus();
        InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));

        if (manager.showSoftInput(view, InputMethodManager.SHOW_FORCED))
            Log.e("------->", "弹出软键盘失败!!");
//        showSoftInputFromWindow(MainActivity.this, mAlbumEditText);
    }

    private void showSoftInputFromWindow(Activity activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        //显示软键盘
//        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //如果上面的代码没有弹出软键盘 可以使用下面另一种方式
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    private void toggleSoftInputFromWindow() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void hideSoftInputFromWindow(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
