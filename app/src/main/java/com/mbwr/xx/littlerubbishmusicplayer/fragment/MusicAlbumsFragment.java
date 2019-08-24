package com.mbwr.xx.littlerubbishmusicplayer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.mbwr.xx.littlerubbishmusicplayer.activity.AlbumSongListActivity;
import com.mbwr.xx.littlerubbishmusicplayer.adapter.AlbumExpandableListAdapter;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Group;
import com.mbwr.xx.littlerubbishmusicplayer.service.PhoneListenerService;
import com.mbwr.xx.littlerubbishmusicplayer.utils.DialogTools;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MusicAlbumsFragment extends Fragment {

    private static final String TAG = MusicAlbumsFragment.class.getSimpleName();
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

    private FragmentActivity activity;

    /**
     * @author xuxiong
     * @time 8/19/19  2:56 AM
     * @describe 重写CreateView方法, 加载界面
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        activity = getActivity();
        Intent intent = new Intent(Utils.getContext(), PhoneListenerService.class);
        getContext().startService(intent);

        //请求储存卡权限
        verifyStoragePermissions(activity);

        musicApp = (MusicApp) Utils.getContext();
        gData = new ArrayList<>();
        gData.add(new Group("我创建的歌单"));
        gData.add(new Group("我收藏的歌单"));

        albums = musicApp.getLocalAlbum();

        iData = new ArrayList<>();
        iData.add(albums);
        iData.add(new ArrayList<Album>());

        mAlbumListView = view.findViewById(R.id.album_expandable_list_view);
        mAlbumExpandableListAdapter = new AlbumExpandableListAdapter(gData, iData, Utils.getContext()) {
            @Override
            public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                ViewHolderGroup groupHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(activity).inflate(
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

            @Override
            public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                final ViewHolderItem itemHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(activity).inflate(
                            R.layout.exlist_group_item_item, parent, false);
                    itemHolder = new ViewHolderItem();
                    itemHolder.vAlbumImage = convertView.findViewById(R.id.album_img_icon);
                    itemHolder.vAlbumName = convertView.findViewById(R.id.album_name);
                    itemHolder.vDelButton = convertView.findViewById(R.id.album_delete);
                    convertView.setTag(itemHolder);
                } else {
                    itemHolder = (ViewHolderItem) convertView.getTag();
                }
//        int imageId = R.mipmap.ic_launcher;//默认的歌单图片
//        itemHolder.vAlbumImage.setImageResource(imageId);
                itemHolder.vAlbumName.setText(iData.get(groupPosition).get(childPosition).getName());
                itemHolder.vDelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ("所有歌曲".equals(itemHolder.vAlbumName.getText())) {
                            Utils.showToastShort("该歌单不可删除!");
                            return;
                        }
                        DialogTools.createConfirmDialog(activity, null, "是否确认删除歌单:" + itemHolder.vAlbumName.getText() + "?",
                                android.R.string.ok, android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    //确定事件
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (LitePal.delete(Album.class, albums.get(childPosition).getId()) > 0) {
                                            albums.remove(childPosition);
                                            musicApp.setLocalAlbum(albums);
                                            MyNotifyDataSetChanged();
                                            Utils.showToastShort("歌单:" + itemHolder.vAlbumName.getText() + " 已删除!");
                                            Log.e(TAG, "歌单:" + itemHolder.vAlbumName.getText() + " 已删除!");
                                        }
                                    }
                                }, new DialogInterface.OnClickListener() {//取消事件
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }, android.R.drawable.ic_delete).show();
                    }
                });

                return super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
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
                Intent intent1 = new Intent(Utils.getContext(), AlbumSongListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("album", ((Long) iData.get(groupPosition).get(childPosition).getId()).intValue());
                intent1.putExtra("MainActivityOld", bundle);
                startActivity(intent1);

//                MainActivityOld.this.finish();
                return true;
            }
        });

        return view;
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
        final EditText editText = new EditText(activity);
        editText.setHint("歌单标题");
        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
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

        final View view = LayoutInflater.from(activity).inflate(R.layout.popuwindow_add_album, null);
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
                InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.showSoftInput(mAlbumEditText, InputMethodManager.SHOW_FORCED);
            }
        }, 1000);

        mAlbumEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager manager = ((InputMethodManager) Utils.getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
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
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.alpha = 0.8f;//设置背景透明度
        activity.getWindow().setAttributes(layoutParams);
        //设置点击外部消失
        popupWindow.setOutsideTouchable(true);
        //窗口消失事件
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams layoutParams1 = activity.getWindow().getAttributes();
                layoutParams1.alpha = 1f;
                activity.getWindow().setAttributes(layoutParams1);
            }
        });
        //显示控件
//        popupWindow.showAsDropDown(getWindow().getDecorView());
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        mAlbumEditText.requestFocus();
//        InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE));

//        if (manager.showSoftInput(view, InputMethodManager.SHOW_FORCED))
//            MyLog.e("------->", "弹出软键盘失败!!");
        showSoftInputFromWindow(activity, mAlbumEditText);
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
        InputMethodManager imm = (InputMethodManager) Utils.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
