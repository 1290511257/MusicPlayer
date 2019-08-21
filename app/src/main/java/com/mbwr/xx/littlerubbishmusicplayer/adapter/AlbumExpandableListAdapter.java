package com.mbwr.xx.littlerubbishmusicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Group;


import java.util.List;

public class AlbumExpandableListAdapter extends BaseExpandableListAdapter {

    private List<Group> gData;
    private List<List<Album>> iData;
    private Context mContext;

    public AlbumExpandableListAdapter(List<Group> gData, List<List<Album>> iData, Context mContext) {

        this.gData = gData;
        this.iData = iData;
        this.mContext = mContext;
    }

    @Override
    public int getGroupCount() {
        return gData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return iData.get(groupPosition).size();
    }

    @Override
    public Group getGroup(int groupPosition) {
        return gData.get(groupPosition);
    }

    @Override
    public Album getChild(int groupPosition, int childPosition) {
        return iData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    //取得用于显示给定分组的视图. 这个方法仅返回分组的视图对象
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.e("xxxxxxxxx", "getGroupView groupPosition = " + groupPosition);
        return convertView;
    }

    //取得给定分组给定子位置的数据用的视图
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Log.e("xxxxxxxxx", "getChildView groupPosition = " + groupPosition + ";childPosition = " + childPosition);
        return convertView;
    }

    //设置子列表是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public static class ViewHolderGroup {
        public TextView tv_group_name;
        public ImageView tv_group_add;
    }

    public static class ViewHolderItem {
        public ImageView vAlbumImage;
        public TextView vAlbumName;
        public ImageView vDelButton;
    }

}
