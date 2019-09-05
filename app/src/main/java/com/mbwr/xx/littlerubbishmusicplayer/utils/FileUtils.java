package com.mbwr.xx.littlerubbishmusicplayer.utils;

import android.os.Environment;
import android.util.Log;

import com.mbwr.xx.littlerubbishmusicplayer.bluetooth.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    public static final File parentFileDir = Utils.getContext().getExternalFilesDir(
            Environment.DIRECTORY_MUSIC);

    /**
     * @param fileSize  文件总大小
     * @param blockSize 单块传输文件大小
     * @author xuxiong
     * @time 8/26/19  9:30 PM
     * @describe 返回文件分块信息字节数组
     */
    public static byte[] getFileBlockInfo(int fileSize, int blockSize) {
        int totalBlock = (fileSize % blockSize) == 0 ? (fileSize / blockSize) : ((fileSize / blockSize) + 1);
        byte[] res = new byte[totalBlock * 9];
        int index = 0;
        for (int i = 0; i < totalBlock; i++) {
            for (byte b : DataConvertUtils.getByteArray(i * blockSize)) {//文件块起始位索引
                res[index++] = b;
            }
            res[index++] = 0;//是否下载完成标志
            if ((i + 1) == totalBlock && fileSize % blockSize != 0) {
                for (byte b : DataConvertUtils.getByteArray(fileSize % blockSize)) {//最后一块大小
                    res[index++] = b;
                }
            } else {
                for (byte b : DataConvertUtils.getByteArray(blockSize)) {
                    res[index++] = b;
                }
            }
        }
        return res;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  11:03 PM
     * @describe 获取存储是否可写
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  11:03 PM
     * @describe 获取存储是否可读
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * @author xuxiong
     * @time 8/27/19  12:56 AM
     * @describe 给定父路径下创建指定大小空白文件
     */
    public static void createEmptyDownloadFile(String fileName, long length) {

        File file = new File(parentFileDir, fileName + ".download");
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            randomAccessFile.setLength(length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null)
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * @author xuxiong
     * @time 9/4/19  2:49 AM
     * @describe 根据字节数组创建文件
     */
    public static void createFileWithBytes(String fileName, byte[] bytes) {
        File file = new File(parentFileDir, fileName);
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            outputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * @author xuxiong
     * @time 9/3/19  10:25 PM
     * @describe 向指定文件位置写入数据
     */
    public static boolean writeDownloadFileDate(String fileName, int index, byte bytes[]) {
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock fileLock = null;
        try {
            randomAccessFile = new RandomAccessFile(parentFileDir + "/" + fileName + ".download", "rws");
            fileChannel = randomAccessFile.getChannel();
            while (true) {
                try {
                    fileLock = fileChannel.lock();//得到文件锁,否则阻塞
//                    Thread.sleep(1);
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "write::: some other thread is holding the file..." + fileName);
                }
            }
            randomAccessFile.seek(index);
            randomAccessFile.write(bytes);

            return true;
        } catch (FileNotFoundException fileE) {
            Log.e(TAG, fileE.toString());
            return false;
        } catch (IOException ioE) {
            Log.e(TAG, ioE.toString());
            return false;
        } finally {
            try {
                fileLock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author xuxiong
     * @time 9/4/19  1:28 AM
     * @describe 创建用以保存文件下载信息的文件
     */
    public static void createInitDownloadInfo(String fileName, byte bytes[]) {
        File file = new File(parentFileDir, fileName + ".pre");
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (bufferedOutputStream != null)
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
        }
    }

    /**
     * @author xuxiong
     * @time 9/4/19  1:16 AM
     * @describe 更新文件下载信息
     */
    public static void updateDownloadInfo(String fileName, int target, boolean hasWrite) {
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock fileLock = null;
        try {
            randomAccessFile = new RandomAccessFile(parentFileDir + "/" + fileName + ".pre", "rws");
            fileChannel = randomAccessFile.getChannel();

            while (true) {
                try {
                    fileLock = fileChannel.lock();//得到文件锁,否则阻塞
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "write::: some other thread is holding the file..." + fileName);
                }
            }
            target = target / Constants.BLOCK_SIZE;//第 target + 1 块数据
            randomAccessFile.seek(9 * target + 4);
            if (hasWrite) {
                randomAccessFile.write(1);
            } else {
                randomAccessFile.write(0);
            }
        } catch (FileNotFoundException fileE) {
            Log.e(TAG, fileE.toString());
        } catch (IOException ioE) {
            Log.e(TAG, ioE.toString());
        } finally {
            if (fileLock != null)
                try {
                    fileLock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (fileChannel != null)
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (randomAccessFile != null)
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * @param rootPath 文件夹路径
     * @param isRegEx  args是否为正则表达式
     * @param args     过滤文件字符串参数,如mp3,mp4忽略大小写,也可直接写正则表达式
     * @author xuxiong
     * @time 8/1/19  5:23 AM
     * @describe 根据文件名结尾判断文件类型选择过滤
     */
    public static List<String> getTargetTypeFiles(String rootPath, Boolean isRegEx, String... args) {
        List<String> filesPath = new ArrayList<>();
        File[] musicFiles = new File(rootPath).listFiles();//获取目录下文件夹及文件
        if ((musicFiles == null) || (args == null)) {
            return filesPath;
        }
        String regExString = args[0];
        if (!isRegEx) {
            regExString = "";
            for (String type : args) {
                if (type == "") break;
                regExString = regExString + ("|." + type);
            }
            regExString = "(" + regExString.substring(1) + ")$";
            isRegEx = true;
        }
        for (int i = 0; i < musicFiles.length; i++) {
            if (musicFiles[i].isFile() && Pattern.compile(regExString).matcher(musicFiles[i].getName()).find()) {//是文件并且匹配正则
                filesPath.add(musicFiles[i].getAbsolutePath());
            } else if (musicFiles[i].isDirectory()) {//为文件夹递归扫描,并将文件夹中所有所匹配文件添加,忽略特定文件夹
                filesPath.addAll(getTargetTypeFiles(musicFiles[i].getAbsolutePath(), isRegEx, regExString));
            }
        }
        return filesPath;//将文件夹中所有匹配文件抛出
    }

    /**
     * @param rootPath  文件夹路径
     * @param ignoreDir 忽略指定文件夹
     * @param isRegEx   args是否为正则表达式
     * @param args      过滤文件字符串参数,如mp3,mp4忽略大小写,也可直接写正则表达式
     * @author xuxiong
     * @time 8/1/19  7:41 AM
     * @describe 根据文件名结尾判断文件类型选择过滤
     */
    public static List<String> getTargetTypeFiles(String rootPath, String ignoreDir, Boolean isRegEx, String... args) {
        List<String> filesPath = new ArrayList<>();
        File[] musicFiles = new File(rootPath).listFiles();//获取目录下文件夹及文件
        if ((musicFiles == null) || (args == null)) {
            return filesPath;
        }
        String regExString = args[0];
        if (!isRegEx) {
            regExString = "";
            for (String type : args) {
                if (type == "") break;
                regExString = regExString + ("|." + type);
            }
            regExString = "(" + regExString.substring(1) + ")$";
            isRegEx = true;
        }

        for (int i = 0; i < musicFiles.length; i++) {
            if (musicFiles[i].isFile() && musicFiles[i].getName().matches(regExString)) {//是文件并且匹配正则
                filesPath.add(musicFiles[i].getAbsolutePath());
            } else if (musicFiles[i].isDirectory() && !musicFiles[i].getAbsolutePath().endsWith(ignoreDir)) {//为文件夹递归扫描,并将文件夹中所有所匹配文件添加,忽略特定文件夹
                filesPath.addAll(getTargetTypeFiles(musicFiles[i].getAbsolutePath(), ignoreDir, isRegEx, regExString));
            }
        }
        return filesPath;//将文件夹中所有匹配文件抛出
    }
}
