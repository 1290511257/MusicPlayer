package com.mbwr.xx.littlerubbishmusicplayer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;

public class Fragment4 extends Fragment {

    private static final String TAG = Fragment4.class.getSimpleName();
    public Fragment4() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_default,container,false);
        TextView txt_content = view.findViewById(R.id.txt_content);
        txt_content.setText("第四个Fragment");
        Log.e(TAG, "onCreateView");

        return view;
    }
}
