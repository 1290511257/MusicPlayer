package com.mbwr.xx.littlerubbishmusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mbwr.xx.littlerubbishmusicplayer.activity.PlayActivity;
import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

public class ListWidgetProvider extends AppWidgetProvider {

    private static final String TAG = ListWidgetProvider.class.getSimpleName();

    public static final String OPEN_MUSIC_ACTIVITY = "com.mbwr.xx.OPEN_MUSIC_ACTIVITY";
    public static final String NEXT_MUSIC = "com.mbwr.xx.NEXT_MUSIC";
    public static final String LAST_MUSIC = "com.mbwr.xx.LAST_MUSIC";
    public static final String PLAY_MUSIC = "com.mbwr.xx.PLAY_MUSIC";
    public static final String COLLECTION_VIEW_ACTION = "com.oitsme.COLLECTION_VIEW_ACTION";
    public static final String COLLECTION_VIEW_EXTRA = "com.oitsme.COLLECTION_VIEW_EXTRA";

    private TextView mTvDef;

    private SeekBar mSeekBarDef;

    private static Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            hideLoading(Utils.getContext());
            Toast.makeText(Utils.getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(TAG, "ListWidgetProvider onUpdate Start");
        for (int appWidgetId : appWidgetIds) {
            // 获取AppWidget对应的视图
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);

            Intent startAppIntent = new Intent().setAction(OPEN_MUSIC_ACTIVITY);
            //在安卓8.1+中不支持广播静态注册，而动态注册需要启动应用程序，因此设定componment指定对指定包名进行广播
            startAppIntent.setComponent(new ComponentName(context, com.mbwr.xx.littlerubbishmusicplayer.ListWidgetProvider.class));
            PendingIntent startAppPendingIntent = PendingIntent.getBroadcast(context, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.tv_refresh, startAppPendingIntent);

            Intent nextMusicIntent = new Intent().setAction(NEXT_MUSIC);
            PendingIntent nextMusicPendingIntent = PendingIntent.getBroadcast(context, 0, nextMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_next, nextMusicPendingIntent);

            Intent preMusicIntent = new Intent().setAction(LAST_MUSIC);
            PendingIntent preMusicPendingIntent = PendingIntent.getBroadcast(context, 0, preMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_pre, preMusicPendingIntent);

            Intent playMusicIntent = new Intent().setAction(PLAY_MUSIC);
            PendingIntent playMusicPendingIntent = PendingIntent.getBroadcast(context, 0, playMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_play, playMusicPendingIntent);


            // remoteViews.
            // 设置 “ListView” 的adapter。
            // (01) intent: 对应启动 ListWidgetService(RemoteViewsService) 的intent
            // (02) setRemoteAdapter: 设置 gridview的适配器
            //     通过setRemoteAdapter将ListView和ListWidgetService关联起来，
            //    以达到通过 ListWidgetService 更新 ListView的目的
//            Intent serviceIntent = new Intent(context, ListWidgetService.class);
            //serviceIntent.setComponent(new ComponentName(context,com.example.xx.myapplication01.widget.ListWidgetProvider.class));
//            remoteViews.setRemoteAdapter(R.id.lv_device, serviceIntent);

            // 设置响应 “ListView” 的intent模板
            // 说明：“集合控件(如GridView、ListView、StackView等)”中包含很多子元素，如GridView包含很多格子。
            // 它们不能像普通的按钮一样通过 setOnClickPendingIntent 设置点击事件，必须先通过两步。
            // (01) 通过 setPendingIntentTemplate 设置 “intent模板”，这是比不可少的！
            // (02) 然后在处理该“集合控件”的RemoteViewsFactory类的getViewAt()接口中 通过 setOnClickFillInIntent 设置“集合控件的某一项的数据”
//            Intent gridIntent = new Intent();
//            gridIntent.setComponent(new ComponentName(context,com.mbwr.xx.littlerubbishmusicplayer.ListWidgetProvider.class));
//            gridIntent.setAction(COLLECTION_VIEW_ACTION);
//            gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gridIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // 设置intent模板
//            remoteViews.setPendingIntentTemplate(R.id.lv_device, pendingIntent);
            // 调用集合管理器对集合进行更新
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "ListWidgetProvider onReceive");
        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        // 接受MusicWidget的点击事件的广播
        switch (action) {
            case OPEN_MUSIC_ACTIVITY:
                enableMusicActivity(context);
                break;
            case LAST_MUSIC:
                break;
            case PLAY_MUSIC:
                break;
            case NEXT_MUSIC:
                break;
        }
        super.onReceive(context, intent);
    }

    /**
     * 显示加载loading
     */
    private void showLoading(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);
        remoteViews.setViewVisibility(R.id.tv_refresh, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.progress_bar, View.VISIBLE);
        remoteViews.setTextViewText(R.id.tv_refresh, "正在刷新...");//文本更新
        refreshWidget(context, remoteViews, false);
    }

    /**
     * 隐藏加载loading
     */
    private void hideLoading(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);
        remoteViews.setViewVisibility(R.id.progress_bar, View.GONE);
        remoteViews.setTextViewText(R.id.tv_refresh, "刷新");
        refreshWidget(context, remoteViews, false);
    }

    /**
     * @author xuxiong
     * @time 7/25/19  10:12 PM
     * @describe 更新进度条
     */
    private void refreshProgress(Context context) {

    }

    /**
     *  @author xuxiong
     *  @time 7/26/19  1:07 AM
     *  @describe 进入音乐播放界面
     */
    private void enableMusicActivity(Context context){
        Intent intent = new Intent(context, PlayActivity.class);
        context.startActivity(intent);
    }
    /**
     * 刷新Widget
     */
    private void refreshWidget(Context context, RemoteViews remoteViews, boolean refreshList) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, ListWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
        if (refreshList)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.lv_device);
    }


}
