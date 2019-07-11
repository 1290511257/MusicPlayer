package com.mbwr.xx.littlerubbishmusicplayer;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mbwr.xx.littlerubbishmusicplayer.model.Device;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private final static String TAG="Music Widget";

    private Context mContext;

    private int mAppWidgetId;

    private static List<Device> mDevices;

    /**
     * 构造GridRemoteViewsFactory
     */
    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Log.i("getViewAt","position="+position);

        // 获取 item_widget_device.xml 对应的RemoteViews
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_device);

        // 设置 第position位的“视图”的数据
        Device device = mDevices.get(position);
        //  rv.setImageViewResource(R.id.iv_lock, ((Integer) map.get(IMAGE_ITEM)).intValue());
        rv.setTextViewText(R.id.tv_name, device.getName());

        // 设置 第position位的“视图”对应的响应事件
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("Type", 0);
        fillInIntent.putExtra(ListWidgetProvider.COLLECTION_VIEW_EXTRA, position);
        rv.setOnClickFillInIntent(R.id.rl_widget_device, fillInIntent);


        Intent lockIntent = new Intent();
        lockIntent.putExtra(ListWidgetProvider.COLLECTION_VIEW_EXTRA, position);
        lockIntent.putExtra("Type", 1);
        rv.setOnClickFillInIntent(R.id.iv_lock, lockIntent);

        Intent unlockIntent = new Intent();
        unlockIntent.putExtra("Type", 2);
        unlockIntent.putExtra(ListWidgetProvider.COLLECTION_VIEW_EXTRA, position);
        rv.setOnClickFillInIntent(R.id.iv_unlock, unlockIntent);

        return rv;
    }


    /**
     * 初始化ListView的数据
     */
    private void initListViewData() {
        mDevices = new ArrayList<>();
//        mDevices.add(new Device("喜欢你哦", 0));
//        mDevices.add(new Device("Oitsme", 1));
//        mDevices.add(new Device("Hi", 0));
//        mDevices.add(new Device("Hey", 1));
//        mDevices = LitePal.findAll(Device.class);
//        int temp = 0;
    }

    private static int i;
    public static void refresh(){
        i++;
        mDevices.add(new Device("Refresh"+i, 1));
    }

    @Override
    public void onCreate() {
        Log.e(TAG,"onCreate");
        // 初始化“集合视图”中的数据
        initListViewData();
    }

    @Override
    public int getCount() {
        // 返回“集合视图”中的数据的总数
        return mDevices.size();
    }

    @Override
    public long getItemId(int position) {
        // 返回当前项在“集合视图”中的位置
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // 只有一类 ListView
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
        mDevices.clear();
    }

}
