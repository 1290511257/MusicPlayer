package com.mbwr.xx.littlerubbishmusicplayer.utils;


public class TimeUtils {
    /**
     * @author xuxiong
     * @time 7/24/19  11:08 PM
     * @describe 将int型时间转换成  00:00格式时间字符串
     */
    public static String convertIntTime2String(int intTime) {
        int totalSec = intTime / 1000;
        int second = totalSec % 60;
        int min = totalSec / 60;
        String timeRight = (second > 9) ? (second + "") : ("0" + second);
        String timeLeft = (min > 9) ? (min + "") : ("0" + min);
        return timeLeft + ":" + timeRight;
    }


}
