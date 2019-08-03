package com.mbwr.xx.littlerubbishmusicplayer.utils;

import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {


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
