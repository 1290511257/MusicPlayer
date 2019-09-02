package com.mbwr.xx.littlerubbishmusicplayer.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mbwr.xx.littlerubbishmusicplayer.BuildConfig;
import com.mbwr.xx.littlerubbishmusicplayer.model.Album;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class Utils {

    /**
     * 此处为Application的context不会造成内存泄漏
     */
    private static Context context;

    /**
     * 是否为debug版
     */
    private static Boolean sDebug;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
        isDebugBuild();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }

    public static Boolean isDebug() {
        return sDebug;
    }

    /**
     * @return 是否是debug版本
     */
    private static boolean isDebugBuild() {
        if (sDebug == null) {
            try {
                final Class<?> activityThread = Class.forName("android.app.ActivityThread");
                final Method currentPackage = activityThread.getMethod("currentPackageName");
                final String packageName = (String) currentPackage.invoke(null, (Object[]) null);
                final Class<?> buildConfig = Class.forName(packageName + ".BuildConfig");
                final Field DEBUG = buildConfig.getField("DEBUG");
                DEBUG.setAccessible(true);
                sDebug = DEBUG.getBoolean(null);
            } catch (final Throwable t) {
                final String message = t.getMessage();
                if (message != null && message.contains("BuildConfig")) {
                    // Proguard obfuscated build. Most likely a production build.
                    sDebug = false;
                } else {
                    sDebug = BuildConfig.DEBUG;
                }
            }
        }
        return sDebug;
    }

    /**
     *  @author xuxiong
     *  @time 8/25/19  10:18 PM
     *  @describe 判断一个服务是否启动
     */
    public static boolean isServiceRunning(Context mContext, String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static void showToastShort(String showText) {
        Toast.makeText(context, showText, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(String showText) {
        Toast.makeText(context, showText, Toast.LENGTH_LONG).show();
    }
}