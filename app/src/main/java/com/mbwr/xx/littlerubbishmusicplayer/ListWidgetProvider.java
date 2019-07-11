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

import com.mbwr.xx.littlerubbishmusicplayer.utils.Utils;

public class ListWidgetProvider extends AppWidgetProvider implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "WIDGET";

    public static final String REFRESH_WIDGET = "com.oitsme.REFRESH_WIDGET";
    public static final String COLLECTION_VIEW_ACTION = "com.oitsme.COLLECTION_VIEW_ACTION";
    public static final String COLLECTION_VIEW_EXTRA = "com.oitsme.COLLECTION_VIEW_EXTRA";

    private TextView mTvDef;

    private SeekBar mSeekBarDef;

    private static Handler mHandler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            hideLoading(Utils.getContext());
            Toast.makeText(Utils.getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d(TAG, "ListWidgetProvider onUpdate");



        for (int appWidgetId : appWidgetIds) {

            // 获取AppWidget对应的视图
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);

            // 设置响应 “按钮(bt_refresh)” 的intent
            Intent btIntent = new Intent().setAction(REFRESH_WIDGET);
            //在安卓8.1+中不支持广播静态注册，而动态注册需要启动应用程序，因此设定componment指定对指定包名进行广播，应该可以一定程度加快速度，减少消耗
            btIntent.setComponent(new ComponentName(context,com.mbwr.xx.littlerubbishmusicplayer.ListWidgetProvider.class));

            PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.tv_refresh, btPendingIntent);



            // remoteViews.
            // 设置 “ListView” 的adapter。
            // (01) intent: 对应启动 ListWidgetService(RemoteViewsService) 的intent
            // (02) setRemoteAdapter: 设置 gridview的适配器
            //     通过setRemoteAdapter将ListView和ListWidgetService关联起来，
            //    以达到通过 ListWidgetService 更新 ListView的目的
            Intent serviceIntent = new Intent(context, ListWidgetService.class);
            //serviceIntent.setComponent(new ComponentName(context,com.example.xx.myapplication01.widget.ListWidgetProvider.class));

            remoteViews.setRemoteAdapter(R.id.lv_device, serviceIntent);


            // 设置响应 “ListView” 的intent模板
            // 说明：“集合控件(如GridView、ListView、StackView等)”中包含很多子元素，如GridView包含很多格子。
            //     它们不能像普通的按钮一样通过 setOnClickPendingIntent 设置点击事件，必须先通过两步。
            //        (01) 通过 setPendingIntentTemplate 设置 “intent模板”，这是比不可少的！
            //        (02) 然后在处理该“集合控件”的RemoteViewsFactory类的getViewAt()接口中 通过 setOnClickFillInIntent 设置“集合控件的某一项的数据”
            Intent gridIntent = new Intent();
            gridIntent.setComponent(new ComponentName(context,com.mbwr.xx.littlerubbishmusicplayer.ListWidgetProvider.class));
            gridIntent.setAction(COLLECTION_VIEW_ACTION);
            gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gridIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // 设置intent模板
            remoteViews.setPendingIntentTemplate(R.id.lv_device, pendingIntent);
            // 调用集合管理器对集合进行更新
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }


        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("==============","ListWidgetProvider onReceive");
        String action = intent.getAction();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (action.equals(COLLECTION_VIEW_ACTION)) {
            // 接受“ListView”的点击事件的广播
            int type = intent.getIntExtra("Type", 0);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int index = intent.getIntExtra(COLLECTION_VIEW_EXTRA, 0);
            switch (type) {
                case 0:
                    Toast.makeText(context, "item" + index, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(context, "lock"+index, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "unlock"+index, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (action.equals(REFRESH_WIDGET)) {
            // 接受“bt_refresh”的点击事件的广播
            Toast.makeText(context, "刷新...", Toast.LENGTH_SHORT).show();
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context,ListWidgetProvider.class);
            //ListRemoteViewsFactory.refresh();
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),R.id.lv_device);
            mHandler.postDelayed(runnable,2000);
            showLoading(context);
        }
        super.onReceive(context, intent);
    }

    /**
     * 显示加载loading
     *
     */
    private void showLoading(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_musicplay);
        remoteViews.setViewVisibility(R.id.tv_refresh, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.progress_bar, View.VISIBLE);
        remoteViews.setTextViewText(R.id.tv_refresh, "正在刷新...");
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
     * 刷新Widget
     */
    private void refreshWidget(Context context, RemoteViews remoteViews, boolean refreshList) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, ListWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
        if (refreshList)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.lv_device);
    }

    /*
     * SeekBar停止滚动的回调函数
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG,"onStopTrackingTouch");
    }

    /*
     * SeekBar开始滚动的回调函数
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG,"onStartTrackingTouch");
    }

    /*
     * SeekBar滚动时的回调函数
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        Log.d(TAG, "seekid:"+seekBar.getId()+", progess = "+progress);

        switch(seekBar.getId()) {
            case R.id.seekbar_def:{
                // 设置“与系统默认SeekBar对应的TextView”的值
//                mTvDef.setText(getResources().getString(R.string.text_def)+" : "+String.valueOf(seekBar.getProgress()));
                break;
            }
            default:
                break;
        }
    }
}
