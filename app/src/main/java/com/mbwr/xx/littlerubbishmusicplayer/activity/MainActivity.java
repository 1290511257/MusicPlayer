package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.adapter.MusicFragmentPagerAdapter;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,
        ViewPager.OnPageChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView mMusicStatus;
    private TextView mTopText;
    private RadioGroup mRGToolBar;
    private RadioButton mRBFound, mRBMusic, mRBVideo, mRBYuncun, mRBAccount;
    private ViewPager mViewPager;

    private MusicFragmentPagerAdapter mAdapter;

    //几个代表页面的常量
    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;
    public static final int PAGE_FOUR = 3;
    public static final int PAGE_FIVE = 4;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragment);
        mAdapter = new MusicFragmentPagerAdapter(getSupportFragmentManager());
        bindViews();
        mRBMusic.setChecked(true);

        verifyStoragePermissions(this);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    private void bindViews() {
        //top

        mTopText = findViewById(R.id.txt_top_bar);
        mMusicStatus = findViewById(R.id.top_bar_image2);


        mMusicStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent musicPlayIntent = new Intent(Utils.getContext(), MusicPlayActivity.class);
                startActivity(musicPlayIntent);
            }
        });


        //bottom
        mRGToolBar = findViewById(R.id.rg_tab_bar);
        mRBFound = findViewById(R.id.rb_found);
        mRBVideo = findViewById(R.id.rb_video);
        mRBYuncun = findViewById(R.id.rb_yuncun);
        mRBAccount = findViewById(R.id.rb_account);
        mRBMusic = findViewById(R.id.rb_music);


        //定义底部图片大小
//        Drawable drawableFound = getResources().getDrawable(R.drawable.tab_menu_found);
//        drawableFound.setBounds(0, 0, 69, 69);//第一是距左右边距离，第二是距上下边距离，第三图片长度,第四宽度
//        mRBFound.setCompoundDrawables(null, drawableFound, null, null);//只放上面
//
//        Drawable drawableVideo = getResources().getDrawable(R.drawable.tab_menu_video);
//        drawableVideo.setBounds(0, 0, 69, 69);
//        mRBVideo.setCompoundDrawables(null, drawableVideo, null, null);
//
//        Drawable drawableMusic = getResources().getDrawable(R.drawable.tab_menu_albums);
//        drawableMusic.setBounds(0, 0, 69, 69);
//        mRBMusic.setCompoundDrawables(null, drawableMusic, null, null);
//
//        Drawable drawableYuncun = getResources().getDrawable(R.drawable.tab_menu_yuncun);
//        drawableYuncun.setBounds(0, 0, 69, 69);
//        mRBYuncun.setCompoundDrawables(null, drawableYuncun, null, null);
//
//        Drawable drawableAccount = getResources().getDrawable(R.drawable.tab_menu_account);
//        drawableAccount.setBounds(0, 0, 69, 69);
//        mRBAccount.setCompoundDrawables(null, drawableAccount, null, null);

        mRGToolBar.setOnCheckedChangeListener(this);
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(2);
        mViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_found:
                mViewPager.setCurrentItem(PAGE_ONE);
                break;
            case R.id.rb_video:
                mViewPager.setCurrentItem(PAGE_TWO);
                break;
            case R.id.rb_music:
                mViewPager.setCurrentItem(PAGE_THREE);
                break;
            case R.id.rb_yuncun:
                mViewPager.setCurrentItem(PAGE_FOUR);
                break;
            case R.id.rb_account:
                mViewPager.setCurrentItem(PAGE_FIVE);
                break;
        }
    }


    //重写ViewPager页面切换的处理方法
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        MyLog.i(TAG, "-----onPageScrolled------\n" +
//                "position = " + position + "; positionOffset = " + positionOffset + "; positionOffsetPixels = " + positionOffsetPixels);
    }

    /**
     * @author xuxiong
     * @time 8/19/19  4:19 AM
     * @describe 处理页面切换逻辑
     */
    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "-----onPageSelected-----\n" +
                "position = " + position);
        switch (position) {
            case 0:
                mTopText.setText(R.string.search_music);
                break;
            case 1:
                break;
            case 2:
                mTopText.setText(R.string.my_music);
                break;
            case 3:
                break;
            case 4:
                break;
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.i(TAG, "-----onPageScrollStateChanged-----\n" +
                "state = " + state);
        //state的状态有三个，0表示什么都没做，1正在滑动，2滑动完毕
        if (state == 2) {
            switch (mViewPager.getCurrentItem()) {
                case PAGE_ONE:
                    mRBFound.setChecked(true);
                    break;
                case PAGE_TWO:
                    mRBVideo.setChecked(true);
                    break;
                case PAGE_THREE:
                    mRBMusic.setChecked(true);
                    break;
                case PAGE_FOUR:
                    mRBYuncun.setChecked(true);
                    break;
                case PAGE_FIVE:
                    mRBAccount.setChecked(true);
                    break;
            }
        }
    }


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
}
