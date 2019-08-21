package com.mbwr.xx.littlerubbishmusicplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.mbwr.xx.littlerubbishmusicplayer.activity.MainActivity;
import com.mbwr.xx.littlerubbishmusicplayer.fragment.MusicAlbumsFragment;
import com.mbwr.xx.littlerubbishmusicplayer.fragment.MusicSearchFragment;
import com.mbwr.xx.littlerubbishmusicplayer.fragment.Fragment2;
import com.mbwr.xx.littlerubbishmusicplayer.fragment.Fragment4;
import com.mbwr.xx.littlerubbishmusicplayer.fragment.Fragment5;

public class MusicFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = MusicFragmentPagerAdapter.class.getSimpleName();

    private final int PAGER_COUNT = 5;
    private MusicSearchFragment foundFragment;
    private Fragment2 fragment2;
    private MusicAlbumsFragment musicAlbumsFragment;
    private Fragment4 fragment4;
    private Fragment5 fragment5;


    public MusicFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        foundFragment = new MusicSearchFragment();
        fragment2 = new Fragment2();
        fragment4 = new Fragment4();
        fragment5 = new Fragment5();
        musicAlbumsFragment = new MusicAlbumsFragment();
    }


    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup vg, int position) {
        return super.instantiateItem(vg, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.i(TAG, "destroyItem position = " + position);
        super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case MainActivity.PAGE_ONE:
                fragment = foundFragment;
                break;
            case MainActivity.PAGE_TWO:
                fragment = fragment2;
                break;
            case MainActivity.PAGE_THREE:
                fragment = musicAlbumsFragment;
                break;
            case MainActivity.PAGE_FOUR:
                fragment = fragment4;
                break;
            case MainActivity.PAGE_FIVE:
                fragment = fragment5;
                break;
        }
        return fragment;
    }


}

