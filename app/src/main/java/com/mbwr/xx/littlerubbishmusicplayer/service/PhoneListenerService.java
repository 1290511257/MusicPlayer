package com.mbwr.xx.littlerubbishmusicplayer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListenerService extends Service {

    private static String TAG = PhoneListenerService.class.getCanonicalName();
    private TelephonyManager telephonyManager;
    private PhoneListener phoneListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Service被创建时调用
    @Override
    public void onCreate() {
        Log.i(TAG, "电话监听Service创建!");
        super.onCreate();
    }

    //Service被启动时调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "电话监听Service启动!");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneListener = new PhoneListener();
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        return super.onStartCommand(intent, flags, startId);
    }

    //Service被关闭之前回调
    @Override
    public void onDestroy() {
        Log.i(TAG, "电话监听Service关闭!");
        if (telephonyManager != null && phoneListener != null) {
            //取消电话监听状态
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        }
        super.onDestroy();
    }

    public class PhoneListener extends PhoneStateListener {

        private final String TAG = PhoneListener.class.getSimpleName();

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
                    break;
            }

        }
    }
}
