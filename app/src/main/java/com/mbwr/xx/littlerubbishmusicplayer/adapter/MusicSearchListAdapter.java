package com.mbwr.xx.littlerubbishmusicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MusicSearchListAdapter extends BaseAdapter {

    private static final String TAG = MusicSearchListAdapter.class.getSimpleName();

    private List<Song> mSongList;

    private LayoutInflater mInflater;//反射器

    public static Map<Integer, Boolean> isSelected;

    public MusicSearchListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setList(List<Song> list) {
        this.mSongList = list;
        isSelected = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return mSongList.size();
    }

    @Override
    public Song getItem(int position) {
        return mSongList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.music_sereach_item, null);
            holder = new ViewHolder();
            holder.vCheckBox = convertView.findViewById(R.id.check_music);
            holder.vSongName = convertView.findViewById(R.id.music_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song mSong = mSongList.get(position);
        holder.vCheckBox.setChecked(isSelected.get(position));
        holder.vSongName.setText(mSong.getName() + "-" + mSong.getSinger());

        return convertView;
    }

    public class ViewHolder {

        public CheckBox vCheckBox;
        TextView vSongName;

    }
}
