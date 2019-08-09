package com.mbwr.xx.littlerubbishmusicplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.activity.MusicPlayActivity;
import com.mbwr.xx.littlerubbishmusicplayer.service.MusicPlayerManager;
import com.mbwr.xx.littlerubbishmusicplayer.utils.TimeUtils;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

public class ListWidgetProvider extends AppWidgetProvider {

    private static final String TAG = ListWidgetProvider.class.getSimpleName();

    public static final String OPEN_MUSIC_ACTIVITY = "com.mbwr.xx.OPEN_MUSIC_ACTIVITY";

    public static final String PLAY_MODE = "com.mbwr.xx.PLAY_MODE";
    public static final String NEXT_MUSIC = "com.mbwr.xx.NEXT_MUSIC";
    public static final String LAST_MUSIC = "com.mbwr.xx.LAST_MUSIC";
    public static final String PLAY_MUSIC = "com.mbwr.xx.PLAY_MUSIC";

    public static final String UPDATE_MUSIC_INFO = MusicPlayerManager.UPDATE_MUSIC_INFO;  //更新音乐基本信息
    public static final String UPDATE_PROGRESS = MusicPlayerManager.UPDATE_PROGRESS;
    public static final String UPDATE_MODE_STATUS = MusicPlayerManager.UPDATE_MODE_STATUS;

    private static int mProgressMax,playMode;

    @Override
    public void onEnabled(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_MUSIC_INFO);
        intentFilter.addAction(UPDATE_MODE_STATUS);
        intentFilter.addAction(UPDATE_PROGRESS);
        Utils.getContext().registerReceiver(this, intentFilter);

        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(TAG, "onUpdate Start");
        for (int appWidgetId : appWidgetIds) {
            // 获取AppWidget对应的视图
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);

            Intent startAppIntent = new Intent().setAction(OPEN_MUSIC_ACTIVITY);
            //在安卓8.1+中不支持广播静态注册，而动态注册需要启动应用程序，因此设定componment指定对指定包名进行广播
            startAppIntent.setComponent(new ComponentName(context, ListWidgetProvider.class));
            PendingIntent startAppPendingIntent = PendingIntent.getBroadcast(context, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.tb_openApp, startAppPendingIntent);

            Intent playModeIntent = new Intent().setAction(PLAY_MODE);
            PendingIntent playModePendingIntent = PendingIntent.getBroadcast(context, 0, playModeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_play_mode, playModePendingIntent);

            Intent nextMusicIntent = new Intent().setAction(NEXT_MUSIC);
            PendingIntent nextMusicPendingIntent = PendingIntent.getBroadcast(context, 0, nextMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_next, nextMusicPendingIntent);

            Intent preMusicIntent = new Intent().setAction(LAST_MUSIC);
            PendingIntent preMusicPendingIntent = PendingIntent.getBroadcast(context, 0, preMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_pre, preMusicPendingIntent);

            Intent playMusicIntent = new Intent().setAction(PLAY_MUSIC);
            PendingIntent playMusicPendingIntent = PendingIntent.getBroadcast(context, 0, playMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_play, playMusicPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        switch (action) {
            case OPEN_MUSIC_ACTIVITY:
                enableMusicActivity(Utils.getContext());
                break;
            case UPDATE_MUSIC_INFO:
                updateMusicInfo(context,intent);
                break;
            case UPDATE_MODE_STATUS:
                updateModeAndStatus(context,intent);
                break;
            case UPDATE_PROGRESS:
                updateProgress(context,intent);
                break;
        }
        super.onReceive(context, intent);
    }

    /**
     * @author xuxiong
     * @time 7/25/19  10:12 PM
     * @describe 更新播放进度条
     */
    private void updateProgress(Context context, Intent intent) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);
        Bundle bd = intent.getExtras();
        int mProgress = bd.getInt("currentPosition");
        remoteViews.setProgressBar(R.id.music_progress,mProgressMax,mProgress,false);
        remoteViews.setTextViewText(R.id.widget_duration_played, TimeUtils.convertIntTime2String(mProgress));
        refreshWidget(context, remoteViews);
    }

    /**
     * @author xuxiong
     * @time 7/29/19  1:19 AM
     * @describe 更新播放音乐信息
     */
    private void updateMusicInfo(Context context, Intent intent) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);
        Bundle bd = intent.getExtras();
        mProgressMax = bd.getInt("duration");
        int mProgress = bd.getInt("currentPosition");

        remoteViews.setTextViewText(R.id.song_name, bd.getString("songName"));
        remoteViews.setTextViewText(R.id.singer_name, bd.getString("singer"));
        remoteViews.setTextViewText(R.id.widget_duration_played, TimeUtils.convertIntTime2String(mProgress));
        remoteViews.setTextViewText(R.id.widget_duration_total, TimeUtils.convertIntTime2String(mProgressMax));
        remoteViews.setProgressBar(R.id.music_progress,mProgressMax,0,false);

        refreshWidget(context, remoteViews);
    }

    /**
     * @author xuxiong
     * @time 7/29/19  1:19 AM
     * @describe 更新播放模式和播放状态
     */
    private void updateModeAndStatus(Context context, Intent intent) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);
        Bundle bd = intent.getExtras();

        playMode = bd.getInt("playMode");
        boolean isPlaying = bd.getBoolean("playStatu");
        switch (playMode){//1顺序循环 2随机循环 3单曲循环
            case 1:
                remoteViews.setImageViewResource(R.id.widget_play_mode,R.drawable.play_icn_loop);
                break;
            case 2:
                remoteViews.setImageViewResource(R.id.widget_play_mode,R.drawable.play_icn_shuffle);
                break;
            case 3:
                remoteViews.setImageViewResource(R.id.widget_play_mode,R.drawable.play_icn_one);
                break;
        }

        if(isPlaying){
            remoteViews.setImageViewResource(R.id.widget_play,R.drawable.widget_play_selector);
        }else {
            remoteViews.setImageViewResource(R.id.widget_play,R.drawable.widget_pause_selector);
        }
        refreshWidget(context, remoteViews);
    }

    /**
     * @author xuxiong
     * @time 7/26/19  1:07 AM
     * @describe 进入音乐播放界面
     */
    private void enableMusicActivity(Context context) {
        Intent intent = new Intent(context, MusicPlayActivity.class);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     *  @author xuxiong
     *  @time 8/7/19  7:21 AM
     *  @describe 刷新Widget
     */
    private void refreshWidget(Context context, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, ListWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
}
