package com.mbwr.xx.littlerubbishmusicplayer.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MToast {

    private static Toast mToast;



    private static TextView tv_content;



    public static void showToast(Context context, String msg) {

        try {

            if (mToast == null) {

//                mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
//
//                mToast.setGravity(Gravity.TOP, 0,
//
//                        DensityUtil.dip2px(context, 3));
//
//                View view = View.inflate(context, R.layout.m_toast, null);
//
//                tv_content = view.findViewById(R.id.tv_content);
//
//                mToast.setView(view);
//
//                tv_content.setText(msg);

            } else {

                tv_content.setText(msg);

            }

            mToast.show();

        } catch (Exception e) {

            // TODO: handle exception

        }

    }

}