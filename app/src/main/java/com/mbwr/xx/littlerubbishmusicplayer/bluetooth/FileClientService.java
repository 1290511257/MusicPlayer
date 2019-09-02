package com.mbwr.xx.littlerubbishmusicplayer.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class FileClientService extends Thread {

    private BluetoothSocket socket;


    public FileClientService(BluetoothSocket socket) {
    }

    public static Handler fileClientHander = new Handler() {
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



}
