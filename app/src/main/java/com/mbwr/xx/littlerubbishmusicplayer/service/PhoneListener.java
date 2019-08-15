package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListener extends PhoneStateListener {

    private static final String TAG = PhoneListener.class.getSimpleName();

    private static boolean aBoolean = true;//只有在电话来后变为IDLE才重新播放音乐

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE: //无任何状态时
                Log.i(TAG, "CALL_STATE_IDLE");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK: //接通
                Log.i(TAG, "CALL_STATE_OFFHOOK");
                break;
            case TelephonyManager.CALL_STATE_RINGING://响铃
                Log.i(TAG, "CALL_STATE_RINGING");
                Message message = new Message();
                message.what = 0;
                MusicPlayerManager.phoneListenerHander.sendMessage(message);
//                MusicPlayerManager musicPlayerManager = MusicPlayerManager.getInstance();
//                musicPlayerManager.OnPause();
                break;
        }

    }
}
