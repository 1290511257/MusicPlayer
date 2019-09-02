package com.mbwr.xx.littlerubbishmusicplayer.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class FileCustomService extends Service {

    private BluetoothSocket mSocket;

    public FileCustomService(BluetoothSocket socket) {
        this.mSocket = socket;

    }

    public static Handler fileCustomHander = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);




        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
