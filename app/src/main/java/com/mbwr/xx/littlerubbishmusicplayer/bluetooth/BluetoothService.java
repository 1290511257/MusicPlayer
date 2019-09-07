package com.mbwr.xx.littlerubbishmusicplayer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.model.Song;
import com.mbwr.xx.littlerubbishmusicplayer.utils.DataConvertUtils;
import com.mbwr.xx.littlerubbishmusicplayer.utils.FileUtils;

import org.litepal.LitePal;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
    private DownloadDateProcessThread mFileReceiveThread;
    private ExecutorService mFileExecutorService;

    private static Map<String, Thread> mDownloadDataDistribution = new HashMap<>();


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
            byte[] bufferHead = new byte[3];
            byte[] bufferSize = new byte[4];
            byte[] bufferContent = null;
            int contentSize;
            byte[] fileNameSize;
            byte[] fileNameBytes;
            String fileName;
            byte[] filePathSize;
            byte[] filePathBytes;
            while (mState == STATE_CONNECTED) {//读取数据
                try {

                    readDate(mmInStream, bufferHead, 0, 3);
                    Log.i(">>>", new String(bufferHead));
                    switch (new String(bufferHead)) {
                        //Request为接收到的请求,Response为请求的回应处理
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
                            Log.i(TAG, data);
                            break;
                        case Constants.MESSAGE_REQUEST_DOWNLOAD_FILE://服务端处理请求下载文件;008
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
                            String path1 = "/storage/emulated/0/Music/Taylor Swift - Look What You Made Me Do.mp3";
                            String path2 = "/storage/emulated/0/Music/Taylor Swift - Sparks Fly.mp3";
                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileResponseTask(path));
                            mFileExecutorService.submit(new FileResponseTask(path1));
                            mFileExecutorService.submit(new FileResponseTask(path2));
                            break;
                        case Constants.MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE://客户端接收下载文件信息;009
                            filePathSize = new byte[4];
                            readDate(mmInStream, filePathSize, 0, filePathSize.length);
                            filePathBytes = new byte[DataConvertUtils.getInt(filePathSize, 0)];
                            readDate(mmInStream, filePathBytes, 0, filePathBytes.length);

                            byte[] fileLen = new byte[4];
                            readDate(mmInStream, fileLen, 0, 4);

                            readDate(mmInStream, bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            bufferContent = new byte[contentSize];
                            readDate(mmInStream, bufferContent, 0, contentSize);
                            String filePath = new String(filePathBytes);
                            fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                            //创建文件下载信息的文本文件
                            FileUtils.createInitDownloadInfo(fileName, bufferContent);
                            FileUtils.createEmptyDownloadFile(fileName, DataConvertUtils.getInt(fileLen, 0));

                            //请求下载整个文件
                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileRequestTask(filePath));
                            int m = 3 + 4 + DataConvertUtils.getInt(filePathSize, 0) + 4 + contentSize;
                            Log.i("==================", "接收到共: " + m + " 字节数据!剩余:" + mmInStream.available() + "字节未读取.");
                            break;
                        case Constants.MESSAGE_REQUEST_DOWNLOAD_FILE_BLOCK://处理文件数据下载请求
                            filePathSize = new byte[4];
                            readDate(mmInStream, filePathSize, 0, filePathSize.length);
                            filePathBytes = new byte[DataConvertUtils.getInt(filePathSize, 0)];
                            readDate(mmInStream, filePathBytes, 0, filePathBytes.length);
                            readDate(mmInStream, bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            bufferContent = new byte[contentSize];
                            readDate(mmInStream, bufferContent, 0, contentSize);
                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileResponseTask(new String(filePathBytes), bufferContent));
                            break;
                        case Constants.MESSAGE_RESPONSE_DOWNLOAD_FILE_BLOCK://接收文件下载数据分发处理;011
                            fileNameSize = new byte[4];
                            readDate(mmInStream, fileNameSize, 0, fileNameSize.length);
                            fileNameBytes = new byte[DataConvertUtils.getInt(fileNameSize, 0)];
                            readDate(mmInStream, fileNameBytes, 0, fileNameBytes.length);
                            fileName = new String(fileNameBytes);
                            //文件块索引
                            byte[] indexBytes = new byte[4];
                            readDate(mmInStream, indexBytes, 0, 4);

                            readDate(mmInStream, bufferSize, 0, bufferSize.length);
                            contentSize = DataConvertUtils.getInt(bufferSize, 0);
                            //文件块内容
                            bufferContent = new byte[contentSize];
                            readDate(mmInStream, bufferContent, 0, contentSize);
                            if (mDownloadDataDistribution.containsKey(fileName)) {
                                DownloadDateProcessThread receiveThread = (DownloadDateProcessThread) mDownloadDataDistribution.get(fileName);
                                if (receiveThread.isAlive() && fileName.equals(receiveThread.fileName)) {
                                    try {
                                        receiveThread.blockingQueue.offer(receiveThread.new FileDate(DataConvertUtils.getInt(indexBytes, 0), bufferContent), 200, TimeUnit.SECONDS);//数据加入处理队列
                                    } catch (InterruptedException e) {
                                        Log.e(TAG, "加入队列失败");
                                        e.printStackTrace();
                                    }
                                } else {
                                    DownloadDateProcessThread downloadDateProcessThread = new DownloadDateProcessThread(new String(fileNameBytes), DataConvertUtils.getInt(indexBytes, 0), bufferContent);
                                    downloadDateProcessThread.start();
                                    mDownloadDataDistribution.put(fileName, downloadDateProcessThread);
                                }
                            } else {
                                DownloadDateProcessThread downloadDateProcessThread = new DownloadDateProcessThread(new String(fileNameBytes), DataConvertUtils.getInt(indexBytes, 0), bufferContent);
                                downloadDateProcessThread.start();
                                mDownloadDataDistribution.put(fileName, downloadDateProcessThread);
                            }
                            break;
                        case Constants.TEMP_ORDER:
                            String pathm = "/storage/emulated/0/Music/Taylor Swift - Gorgeous.mp3";
                            //请求继续下载文件
                            if (null == mFileExecutorService)
                                mFileExecutorService = Executors.newFixedThreadPool(20);
                            mFileExecutorService.submit(new FileRequestTask(pathm));
                            break;
                        case Constants.FILE_DOWNLOAD_END:
                            filePathSize = new byte[4];
                            readDate(mmInStream, filePathSize);
                            filePathBytes = new byte[DataConvertUtils.getInt(filePathSize, 0)];
                            readDate(mmInStream, filePathBytes);
                            filePath = new String(filePathBytes);
                            FileDownloadedCheck fileDownloadedCheck = new FileDownloadedCheck(filePath);
                            if ((boolean) fileDownloadedCheck.call()) {
                                Log.i(TAG, "下载完成");
                            } else {
                                if (null == mFileExecutorService)
                                    mFileExecutorService = Executors.newFixedThreadPool(20);
                                mFileExecutorService.submit(new FileRequestTask(filePath));
                            }
                        default:
//                            size = mmInStream.available();
//                            byte[] bytes1 = new byte[size];
//                            bytes1 = readDate(mmInStream, bytes1, 0, size);
                            Log.e("未知数据", "共" + mmInStream.available() + " byte 未知数据.");
                    }

                    //检索数据包结束指令,用以过滤数据包不完整情况,使下一次读取正常
                    byte[] result = new byte[3];
                    while (!Constants.MASSAGE_END_INDEX.equals(new String(result))) {
                        int newByte;
                        if ((newByte = mmInStream.read()) != -1) {
                            result[0] = result[1];
                            result[1] = result[2];
                            result[2] = (byte) newByte;
                        }
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] buffer) {
            try {
                byte[] endIndex = Constants.MASSAGE_END_INDEX.getBytes();//数据包结束标志
                mmOutStream.write(DataConvertUtils.byteArrayAdd(buffer, endIndex));
                if (buffer.length < 50) {//用于显示传输的数据,正式使用后删除
                    mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                } else {
                    byte[] order = new byte[3];
                    order[0] = buffer[0];
                    order[1] = buffer[1];
                    order[2] = buffer[2];
                    String msg = ">" + new String(order) + ": 写入数据流 " + buffer.length + "个字节";
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
    private class DownloadDateProcessThread extends Thread {

        private String fileName;
        private BlockingQueue<FileDate> blockingQueue;

        DownloadDateProcessThread(String file, int index, byte[] bytes) {
            this.fileName = file;
            this.blockingQueue = new LinkedBlockingQueue();
            if (!this.blockingQueue.offer(new FileDate(index, bytes)))
                this.blockingQueue.offer(new FileDate(index, bytes));//如果加入队列失败,则尝试再次加入
        }

        @Override
        public void run() {
            while (true) {
                FileDate fileDate = null;
                try {
                    fileDate = blockingQueue.take();//从队列中取数据,无数据则阻塞
                    Log.i(TAG, ">>> 文件: " + fileName + "正在下载 当前进度:" + fileDate.index);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (null != fileDate) {
                    if (FileUtils.writeDownloadFileDate(fileName, fileDate.index, fileDate.bytes))//写入传输数据文件
                        FileUtils.updateDownloadInfo(fileName, fileDate.index, true);//更新下载信息
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "文件块保存失败!");
                        e.printStackTrace();
                    }
                }
            }

        }

        class FileDate {
            private int index;
            private byte[] bytes;

            public FileDate(int index, byte[] bytes) {
                this.index = index;
                this.bytes = bytes;
            }
        }
    }

    /**
     * @author xuxiong
     * @time 9/5/19  3:53 AM
     * @describe 检查文件是否成功传完
     */
    private class FileDownloadedCheck extends Thread implements Callable {

        private String fileName;
        private RandomAccessFile randomAccessFile;
        private boolean isDone = true;

        FileDownloadedCheck(String filePath) {
            this.fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            ;
        }

        @Override
        public void run() {//检查文件是否接收完成
            try {
                randomAccessFile = new RandomAccessFile(FileUtils.parentFileDir + "/" + fileName + ".pre", "r");
                for (int i = 4; i < randomAccessFile.length(); i += 9) {
                    randomAccessFile.seek(i);
                    if (randomAccessFile.read() == 0) {
                        isDone = false;
                        break;
                    }
                }
                randomAccessFile.close();
                if (isDone) {
                    File preFile = new File(FileUtils.parentFileDir + "/" + fileName + ".pre");
                    if (preFile.exists() && preFile.isFile() && preFile.delete()) {
                        File file = new File(FileUtils.parentFileDir + "/" + fileName + ".download");
                        file.renameTo(new File(FileUtils.parentFileDir + "/" + fileName));
                        Log.i(TAG, "下载信息删除成功!");
                    } else {
                        Log.i(TAG, preFile.getName() + "文件不存在或删除失败!");
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Object call() throws Exception {
            return isDone;
        }
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
                        //文件分块信息
                        bytes = FileUtils.getFileBlockInfo(fileLength.intValue(), Constants.BLOCK_SIZE);
                        // 3 + 4 :remoteFilePath length + n : remoteFilePath + 4 : data size + n1 : data
                        byte[] response = DataConvertUtils.byteArrayAdd(Constants.MESSAGE_RESPONSE_PRE_DOWNLOAD_FILE.getBytes(),
                                DataConvertUtils.getByteArray(file.getPath().length()),
                                file.getPath().getBytes(),
                                DataConvertUtils.getByteArray(fileLength.intValue()),
                                DataConvertUtils.getByteArray(bytes.length),
                                bytes);

                        write(response);
                    } else {
                        write(Constants.MESSAGE_SOURCE_NOT_EXIST.getBytes());
                    }
                    break;
                case 2://发送文件数据
                    int l = bytes.length;
                    int index, blockSize;
                    for (int i = 4; i < l; i = i + 9) {
                        if (bytes[i] == 0) {//如果该块数据未下载
                            index = DataConvertUtils.getInt(bytes, i - 4);
                            blockSize = DataConvertUtils.getInt(bytes, i + 1);
                            byte[] data = new byte[blockSize];
                            try {
                                randomAccessFile.seek(index);
                                randomAccessFile.read(data, 0, blockSize);//读取指定位置指定大小数据
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
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

                        }
                    }
                    //服务端传输完成
                    byte[] fileDownloadOK = DataConvertUtils.byteArrayAdd(
                            Constants.FILE_DOWNLOAD_END.getBytes(),
                            DataConvertUtils.getByteArray(file.getPath().getBytes().length),
                            file.getPath().getBytes());
                    write(fileDownloadOK);
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
    private class FileRequestTask implements Runnable {//请求下载文件

        private FileInputStream inputStream;
        private String remoteFilePath;
        private byte[] bytes;
        private int operator;
        private File localFile;

        FileRequestTask(String filePath) {//filePath为服务端文件存放路径
            this.localFile = new File(FileUtils.parentFileDir + "/" + filePath.substring(filePath.lastIndexOf('/') + 1) + ".pre");//本地下载信息文件
            this.remoteFilePath = filePath;
            this.operator = 2;
        }

        public void run() {
            switch (operator) {
                case 1:
                    break;
                case 2:
                    Long l = localFile.length();
                    bytes = new byte[l.intValue()];
                    if (localFile.exists() && localFile.isFile()) {
                        try {
                            inputStream = new FileInputStream(localFile);
                            inputStream.read(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.e(TAG, "文件" + localFile.getPath() + "不存在");
                    }

                    byte[] fileBlockRequest = DataConvertUtils.byteArrayAdd(
                            Constants.MESSAGE_REQUEST_DOWNLOAD_FILE_BLOCK.getBytes(),
                            DataConvertUtils.getByteArray(remoteFilePath.getBytes().length),
                            remoteFilePath.getBytes(),
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

    /**
     * @param inputStream 字节输入流
     * @param b           将读取数据保存至数组 b
     * @param off         从 b[off]处开始写入数据
     * @param length      读取长度
     * @author xuxiong
     * @time 9/3/19  6:18 AM
     * @describe 该方法保证从流中读取指定大小数据, 未读取到指定数据将阻塞
     */
    private void readDate(InputStream inputStream, @Nullable byte b[], int off, int length) throws IOException, InterruptedException {
        int readMaxCount;
        int n = 0;
        do {
            readMaxCount = Constants.MAX_SIZE_READ_INPUT;
            if (readMaxCount > (length - n))
                readMaxCount = length - n;
            int count = inputStream.read(b, off + n, readMaxCount);
            if (count < 0)
                throw new EOFException();
            n += count;
            //休眠时间,配合 mMaxReadSize 实现流量控制
            Thread.sleep(Constants.READ_DATE_SLEEP_TIME);
        } while (n < length);

    }

    /**
     * @param inputStream 字节输入流
     * @param b           将读取数据保存至数组 b
     * @author xuxiong
     * @time 9/4/19  4:55 AM
     * @describe 从输入流中读取数据, 未读满数组将阻塞
     */
    private void readDate(InputStream inputStream, @Nullable byte b[]) throws IOException, InterruptedException {
        readDate(inputStream, b, 0, b.length);
    }

}
