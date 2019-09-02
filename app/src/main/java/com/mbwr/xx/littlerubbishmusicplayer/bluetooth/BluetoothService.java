package com.mbwr.xx.littlerubbishmusicplayer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.utils.DataConvertUtils;
import com.mbwr.xx.littlerubbishmusicplayer.utils.FileUtils;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothService {

    private static final String TAG = BluetoothService.class.getSimpleName();

    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

//    00001106-0000-1000-8000-00805F9B34FB

    private static final UUID UUID_SECURE =
            UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");//文件传输服务UUID
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");

//    private static final UUID UUID_SECURE =
//            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//    private static final UUID UUID_INSECURE =
//            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private FileListSend mFileListSend;
    private FileListReceive mFileListReceive;
    private FileResponseTask mFileSendThread;
    private FileReceiveThread mFileReceiveThread;
    private ExecutorService mFileExecutorService;


    private int mState;
    private int mNewState;

    public static final int STATE_NONE = 0;       // do nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
        updateUserInterfaceTitle();
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        updateUserInterfaceTitle();
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {

        Log.d(TAG, "connected, Socket Type:" + socketType);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        updateUserInterfaceTitle();
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
        updateUserInterfaceTitle();
    }

    public void write(byte[] out) {

        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
            r.write(out);
        }
    }

    private void connectionFailed() {

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        updateUserInterfaceTitle();

        BluetoothService.this.start();
    }

    private void connectionLost() {
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        updateUserInterfaceTitle();

        BluetoothService.this.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "  accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * @author xuxiong
     * @time 8/24/19  2:45 AM
     * @describe 数据传输线程
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] bufferHead = new byte[3];
            byte[] bufferSize = new byte[4];
            byte[] bufferContent = null;
            int contentSize;
            int size = 0;
            while (mState == STATE_CONNECTED) {//读取数据
                try {
                    mmInStream.read(bufferHead, 0, 3);
                    Log.i("====请求指令====", new String(bufferHead));
                    switch (new String(bufferHead)) {
                        //Request为接收到的请求,Response为客户端的回应数据
                        case Constants.MESSAGE_REQUEST_FILE_LIST://请求文件列表
                            Log.i(TAG, "RUN MESSAGE_REQUEST_FILE_LIST");
                            mFileListSend = new FileListSend();
                            mFileListSend.start();
                            break;
                        case Constants.MESSAGE_RESPONSE_FILE_LIST://回应文件列表请求
                            Log.i(TAG, "RUN MESSAGE_RESPONSE_FILE_LIST");
                            mmInStream.read(bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            bufferContent = new byte[contentSize];
                            mmInStream.read(bufferContent, 0, contentSize);
                            String data = new String(bufferContent);
                            Log.i("=============", data);
                            break;
                        case Constants.MESSAGE_REQUEST_DOWNLOAD_FILE://服务端处理请求下载文件
                            Log.i(TAG, "RUN MESSAGE_REQUEST_DOWNLOAD_FILE");
                            //预调试时无法使用
//                            mmInStream.read(bufferSize, 0, bufferSize.length);
//                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
//                            bufferContent = new byte[contentSize];
//                            mmInStream.read(bufferContent, 0, contentSize);
//                            String data1 = new String(bufferContent);//得到传输对应的数据,请求文件路径
//                            mFileSendThread = new FileResponseTask(data1);
//                            Log.i("==========>>>", data1);

                            //>>>>>>test
                            String path = "/storage/emulated/0/Music/Taylor Swift - Gorgeous.mp3";
                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileResponseTask(path));
                            break;
                        case Constants.MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE://客户端接收下载文件信息;009
                            Log.i(TAG, "RUN MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE");
                            byte[] filePathSize = new byte[4];
                            mmInStream.read(filePathSize, 0, filePathSize.length);

                            byte[] filePathBytes = new byte[DataConvertUtils.getInt(filePathSize, 0)];
                            mmInStream.read(filePathBytes, 0, filePathBytes.length);

                            mmInStream.read(bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            bufferContent = new byte[contentSize];
                            mmInStream.read(bufferContent, 0, contentSize);

                            String filePath = new String(filePathBytes);
                            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.') + 1) + "pre";
                            FileUtils.createFileWithByte(bufferContent, fileName);

                            int ttt = mmInStream.available();
                            //请求下载整个文件
                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileRequestTask(filePath, bufferContent));
                            int m = 3 + 4 + DataConvertUtils.getInt(filePathSize, 0) + 4 + contentSize;
                            Log.i("==================", "接收到共: " + m + " 字节数据!剩余:" + ttt + "字节未读取.");
                            Log.i("=======>>>>>>", "MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE");
                            break;
                        case Constants.MESSAGE_REQUEST_DOWNLOAD_FILE_BLOCK://处理文件数据下载请求
                            Log.i(TAG, "RUN MESSAGE_REQUEST_DOWNLOAD_FILE_BLOCK");

                            byte[] filePathSize1 = new byte[4];
                            mmInStream.read(filePathSize1, 0, filePathSize1.length);

                            byte[] filePathBytes1 = new byte[DataConvertUtils.getInt(filePathSize1, 0)];
                            mmInStream.read(filePathBytes1, 0, filePathBytes1.length);

                            mmInStream.read(bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            bufferContent = new byte[contentSize];
                            mmInStream.read(bufferContent, 0, contentSize);

                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileResponseTask(new String(filePathBytes1), bufferContent));
                            break;
                        case Constants.MESSAGE_RESPONSE_DOWNLOAD_FILE_BLOCK://接收文件下载数据
                            Log.i(TAG, "RUN MESSAGE_RESPONSE_DOWNLOAD_FILE_BLOCK");
                            byte[] fileNameSize = new byte[4];
                            mmInStream.read(fileNameSize, 0, fileNameSize.length);
                            byte[] fileNameBytes = new byte[DataConvertUtils.getInt(fileNameSize, 0)];
                            mmInStream.read(fileNameBytes, 0, fileNameBytes.length);
                            byte[] indexBytes = new byte[4];
                            mmInStream.read(indexBytes, 0, 4);
                            mmInStream.read(bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            bufferContent = new byte[contentSize];
                            mmInStream.read(bufferContent, 0, contentSize);
                            String string = new String(fileNameBytes);
                            Log.i("===========", "fileName = " + string);


                            for (int l = contentSize - 100; l < contentSize; l++) {
                                Log.e("MMMMMMMMMMMMM", "byte = " + bufferContent[l]);
                            }
                            int ttt1 = mmInStream.available();
                            int m1 = 3 + 4 + DataConvertUtils.getInt(fileNameSize, 0) + 4 + 4 + contentSize;
                            Log.i("============", "接收到共: " + m1 + " 字节数据!剩余:" + ttt1 + "字节未读取.");
                            break;
                        default:
                            mmInStream.skip(64582);
                            size = mmInStream.available();
                            Log.i("========", "该次传输后剩余 : " + size + " byte 未知数据.");
                    }

                    if (bufferContent != null) {
//                        mHandler.obtainMessage(Constants.MESSAGE_READ, bufferContent.length, -1, bufferContent)
//                                .sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {

                if (buffer.length < 50) {//用于显示传输的数据,正式使用后注释掉
                    mmOutStream.write(buffer);
                    mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                } else {
                    byte[] order = new byte[3];
                    order[0] = buffer[0];
                    order[1] = buffer[1];
                    order[2] = buffer[2];
                    String msg = ">" + new String(order) + ": 写入数据流 " + buffer.length + "个字节";
                    if (buffer.length <= Constants.MAX_SIZE_WRITE_INPUT) {
                        mmOutStream.write(buffer);
                    } else {
                        int i = 0;
                        for (; i < buffer.length; i += Constants.MAX_SIZE_WRITE_INPUT) {
                            mmOutStream.write(buffer, i, Constants.MAX_SIZE_WRITE_INPUT);
                        }
                        if ((i -= 1024) < buffer.length) {
                            mmOutStream.write(buffer, i, buffer.length - i);
                        }
                    }
                    mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, msg.getBytes())
                            .sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * @author xuxiong
     * @time 8/25/19  10:52 PM
     * @describe 文件接收线程
     */
    private class FileReceiveThread extends Thread {

    }

    /**
     * @author xuxiong
     * @time 8/25/19  10:52 PM
     * @describe 回应文件线程;服务端
     */
    private class FileResponseTask implements Runnable {

        private RandomAccessFile randomAccessFile;
        private File file;
        private byte[] bytes = null;//前四位表索引,中间一位表状态,后四位表大小,9位一循环
        private int operator;
        private String fileName;

        public FileResponseTask(String path) throws FileNotFoundException {
            this.file = new File(path);
            this.operator = 1;
            this.fileName = file.getName();
            this.randomAccessFile = new RandomAccessFile(path, "r");
        }

        public FileResponseTask(String path, byte[] bytes) throws FileNotFoundException {
            this.fileName = (new File(path)).getName();
            this.randomAccessFile = new RandomAccessFile(path, "r");
            this.operator = 2;
            this.bytes = bytes;
        }

        public void run() {
            Log.i(TAG, "FileResponseTask");
            switch (operator) {
                case 1://发送文件信息
                    Long fileLength = file.length();
                    if (file.exists() && fileLength != 0) {
                        bytes = FileUtils.getFileBlockInfo(fileLength.intValue(), Constants.BLOCK_SIZE);
                        // 3 + 4 :filePath length + n : filePath + 4 : data size + n1 : data
                        byte[] response = DataConvertUtils.byteArrayAdd(Constants.MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE.getBytes(),
                                DataConvertUtils.getByteArray(file.getPath().length()), file.getPath().getBytes(),
                                DataConvertUtils.getByteArray(bytes.length), bytes);
                        write(response);
                        Log.i("=============", "pre file包共发送:" + response.length + " byte 数据");
                        Log.i(TAG, "FileResponseTask write file pre info end....");
                    } else {
                        write(Constants.MESSAGE_SOURCE_NOT_EXIST.getBytes());
                    }
                    break;
                case 2://发送文件数据
                    int l = bytes.length;
                    int index, blockSize;
                    //15 --> l
                    for (int i = 4; i < 15; i = i + 9) {

                        if (bytes[i] == 0) {//如果该块数据未下载

//                            write(Constants.MESSAGE_RESPONSE_DOWNLOAD_FILE_BLOCK.getBytes());
                            index = DataConvertUtils.getInt(bytes, i - 4);
                            blockSize = DataConvertUtils.getInt(bytes, i + 1);
                            Log.i(">>>>>>>>", "index = " + index);
                            Log.i(">>>>>>>>", "blockSize = " + blockSize);
                            byte[] data = new byte[blockSize];
                            try {
                                randomAccessFile.seek(index);
                                randomAccessFile.read(data, 0, blockSize);//读取指定位置指定大小数据
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                            }

                            for (int l1 = blockSize - 100; l1 < blockSize; l1++) {
                                Log.e("MMMMMMMMMMMMM", "byte = " + data[l1]);
                            }

                            // 3 bit:指令 + 4 bit:文件名长度 + n bit:文件名 + 4 bit:文件块索引 + 4 bit:文件块大小 n1 bit:文件块数据
                            byte[] fileBlockResponse = DataConvertUtils.byteArrayAdd(
                                    Constants.MESSAGE_RESPONSE_DOWNLOAD_FILE_BLOCK.getBytes(),
                                    DataConvertUtils.getByteArray(fileName.getBytes().length),
                                    fileName.getBytes(),
                                    DataConvertUtils.getByteArray(index),
                                    DataConvertUtils.getByteArray(blockSize),
                                    data);
                            write(fileBlockResponse);

                            Log.i(">>>>>>>>", "fileName Size = " + fileName.getBytes().length);
                            Log.i(">>>>>>>>", "fileName = " + fileName);
                            Log.i("========", "file block 包共发送:" + (3 + fileBlockResponse.length) + " byte数据.");
                        }
                        Log.i(TAG, "FileResponseTask write one block end....");
                    }
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3://请求指定块数据
                    break;
            }
        }

    }

    /**
     * @author xuxiong
     * @time 8/27/19  4:42 AM
     * @describe 发送请求类线程;客户端
     */
    private class FileRequestTask implements Runnable {

        private String filePath;
        private byte[] bytes;
        private int operator;

        FileRequestTask(String filePath, byte[] bytes) {
            this.filePath = filePath;
            this.bytes = bytes;
            this.operator = 2;
        }

        public void run() {
            switch (operator) {
                case 1:
                    break;
                case 2:
                    byte[] fileBlockRequest = DataConvertUtils.byteArrayAdd(
                            Constants.MESSAGE_REQUEST_DOWNLOAD_FILE_BLOCK.getBytes(),
                            DataConvertUtils.getByteArray(filePath.getBytes().length),
                            filePath.getBytes(),
                            DataConvertUtils.getByteArray(bytes.length),
                            bytes);
                    write(fileBlockRequest);
                    break;
                case 3:
                    break;
            }
        }
    }

    /**
     * @author xuxiong
     * @time 8/26/19  1:09 AM
     * @describe 文件列表数据接收
     */
    private class FileListReceive extends Thread {

    }

    /**
     * @author xuxiong
     * @time 8/26/19  1:10 AM
     * @describe 文件列表数据发送
     */
    private class FileListSend extends Thread {
        private List<Song> songs;
        private int index = 0;

        FileListSend() {
            songs = LitePal.findAll(Song.class);
        }

        public void run() {
            if (songs.size() == 0) {
                write(Constants.MESSAGE_SOURCE_NOT_EXIST.getBytes());
                return;
            }
            Song song;
            while (index != songs.size()) {//数据以包头,包长,包体方式封装.方便分包
                song = songs.get(index);
                byte[] bytesContent = (song.getName() + ";" + song.getFilePath() + ";" + song.getSize()).getBytes();
                byte[] res = DataConvertUtils.byteArrayAdd(Constants.MESSAGE_RESPONSE_FILE_LIST.getBytes(), DataConvertUtils.getByteArray(bytesContent.length), bytesContent);
                write(res);
                index++;
            }
            index = 0;
        }
    }


}
