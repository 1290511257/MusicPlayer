package com.mbwr.xx.littlerubbishmusicplayer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;

public class Fragment5 extends Fragment {

    private static final String TAG = Fragment5.class.getSimpleName();

    public Fragment5() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_default, container, false);
        TextView txt_content = view.findViewById(R.id.txt_content);
        txt_content.setText("第五个Fragment");

        return view;
    }
}
