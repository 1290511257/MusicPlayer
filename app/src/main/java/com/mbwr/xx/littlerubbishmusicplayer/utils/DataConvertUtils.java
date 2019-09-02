package com.mbwr.xx.littlerubbishmusicplayer.utils;

import java.nio.ByteBuffer;

public class DataConvertUtils {


    private static final char[] HEXES = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    /**
     * @author xuxiong
     * @time 8/26/19  4:45 AM
     * @describe char转换为byte[2]数组
     */
    public static byte[] getByteArray(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xff00) >> 8);
        b[1] = (byte) (c & 0x00ff);
        return b;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:46 AM
     * @describe 从byte数组的index处的连续两个字节获得一个char
     */
    public static char getChar(byte[] arr, int index) {
        return (char) (0xff00 & arr[index] << 8 | (0xff & arr[index + 1]));
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:46 AM
     * @describe short转换为byte[2]数组
     */
    public static byte[] getByteArray(short s) {
        byte[] b = new byte[2];
        b[0] = (byte) ((s & 0xff00) >> 8);
        b[1] = (byte) (s & 0x00ff);
        return b;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:46 AM
     * @describe 从byte数组的index处的连续两个字节获得一个short
     */
    public static short getShort(byte[] arr, int index) {
        return (short) (0xff00 & arr[index] << 8 | (0xff & arr[index + 1]));
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:46 AM
     * @describe int转换为byte[4]数组
     */
    public static byte[] getByteArray(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) ((i & 0xff000000) >> 24);
        b[1] = (byte) ((i & 0x00ff0000) >> 16);
        b[2] = (byte) ((i & 0x0000ff00) >> 8);
        b[3] = (byte) (i & 0x000000ff);
        return b;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:47 AM
     * @describe 从byte数组的index处的连续4个字节获得一个int
     */
    public static int getInt(byte[] arr, int index) {
        return (0xff000000 & (arr[index + 0] << 24)) |
                (0x00ff0000 & (arr[index + 1] << 16)) |
                (0x0000ff00 & (arr[index + 2] << 8)) |
                (0x000000ff & arr[index + 3]);
    }

    /**
     * @author xuxiong
     * @time 8/27/19  4:12 AM
     * @describe byte数组 转换成 16进制小写字符串
     */
    public static String bytes2Hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            hex.append(HEXES[(b >> 4) & 0x0F]);
            hex.append(HEXES[b & 0x0F]);
        }

        return hex.toString();
    }

    /**
     * @author xuxiong
     * @time 8/27/19  4:12 AM
     * @describe 16进制字符串 转换为对应的 byte数组
     */
    public static byte[] hex2Bytes(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        char[] hexChars = hex.toCharArray();
        byte[] bytes = new byte[hexChars.length / 2];   // 如果 hex 中的字符不是偶数个, 则忽略最后一个

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt("" + hexChars[i * 2] + hexChars[i * 2 + 1], 16);
        }

        return bytes;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:47 AM
     * @describe float转换为byte[4]数组
     */
    public static byte[] getByteArray(float f) {
        int intbits = Float.floatToIntBits(f);//将float里面的二进制串解释为int整数
        return getByteArray(intbits);
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:47 AM
     * @describe 从byte数组的index处的连续4个字节获得一个float
     */
    public static float getFloat(byte[] arr, int index) {
        return Float.intBitsToFloat(getInt(arr, index));
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:48 AM
     * @describe long转换为byte[8]数组
     */
    public static byte[] getByteArray(long l) {
        byte b[] = new byte[8];
        b[0] = (byte) (0xff & (l >> 56));
        b[1] = (byte) (0xff & (l >> 48));
        b[2] = (byte) (0xff & (l >> 40));
        b[3] = (byte) (0xff & (l >> 32));
        b[4] = (byte) (0xff & (l >> 24));
        b[5] = (byte) (0xff & (l >> 16));
        b[6] = (byte) (0xff & (l >> 8));
        b[7] = (byte) (0xff & l);
        return b;
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:49 AM
     * @describe 从byte数组的index处的连续8个字节获得一个long
     */
    public static long getLong(byte[] arr, int index) {
        return (0xff00000000000000L & ((long) arr[index + 0] << 56)) |
                (0x00ff000000000000L & ((long) arr[index + 1] << 48)) |
                (0x0000ff0000000000L & ((long) arr[index + 2] << 40)) |
                (0x000000ff00000000L & ((long) arr[index + 3] << 32)) |
                (0x00000000ff000000L & ((long) arr[index + 4] << 24)) |
                (0x0000000000ff0000L & ((long) arr[index + 5] << 16)) |
                (0x000000000000ff00L & ((long) arr[index + 6] << 8)) |
                (0x00000000000000ffL & (long) arr[index + 7]);
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:49 AM
     * @describe double转换为byte[8]数组
     */
    public static byte[] getByteArray(double d) {
        long longbits = Double.doubleToLongBits(d);
        return getByteArray(longbits);
    }

    /**
     * @author xuxiong
     * @time 8/26/19  4:49 AM
     * @describe 从byte数组的index处的连续8个字节获得一个double
     */
    public static double getDouble(byte[] arr, int index) {
        return Double.longBitsToDouble(getLong(arr, index));
    }

    /**
     * @author xuxiong
     * @time 8/26/19  6:49 AM
     * @describe 将多个byte数组相加
     */
    public static byte[] byteArrayAdd(byte[] baseBytes, byte[]... bytesList) {
        byte[] result;
        int length = baseBytes.length;
        for (byte[] b : bytesList) {
            length += b.length;
        }
        result = new byte[length];
        length = 0;
        for (byte b : baseBytes) {
            result[length++] = b;
        }
        for (byte[] bytes : bytesList) {
            for (byte b : bytes) {
                result[length++] = b;
            }
        }
        return result;
    }
}
