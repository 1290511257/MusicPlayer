package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListener extends PhoneStateListener {

    //当电话状态发生改变时回调此方法
    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);
        try {

            switch (state){

                case TelephonyManager.CALL_STATE_IDLE: //无任何状态时
                    Log.i("=================","CALL_STATE_IDLE");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK: //接通
                    Log.i("=================","CALL_STATE_OFFHOOK");
                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃
                    //响铃时的具体操作
                    Log.i("=================","CALL_STATE_RINGING");
                    break;
            }

        }catch (Exception e){
            //Log.i("Err", e.toString());
        }
    }
}
