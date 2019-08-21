package com.mbwr.xx.littlerubbishmusicplayer.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.model.Song;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayListAdapter extends BaseAdapter implements View.OnClickListener {

    private List<Song> playlist;
    private LayoutInflater inflater;//反射器
    private Dialog mDialog;
    private Context context;
    private int from;
    public static Map<Integer, Boolean> isplaying = new HashMap<Integer, Boolean>();
    private Callback mycallback;

    public PlayListAdapter(Context context, int from) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.from = from;
    }

    public void setMycallback(Callback c) {
        mycallback = c;
    }

    public void setPlaylist(List<Song> playlist) {
        this.playlist = playlist;
        for (int i = 0; i < playlist.size(); i++) {
            isplaying.put(i, false);
        }
    }

    public interface Callback {
        void onClick(View v);
    }

    //回调方法
    @Override
    public int getCount() {
        return playlist.size();
    }

    @Override
    public Object getItem(int position) {
        return playlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.playmusiclist,null);
            holder = new ViewHolder();
//            holder.playing= convertView.findViewById(R.id.when_playing);
//            holder.playname= convertView.findViewById(R.id.paly_music_name);
//            holder.delete= convertView.findViewById(R.id.delete_music);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Song song = playlist.get(position);
        if (!song.getName().equals("null")) {
            holder.playname.setText(song.getSinger() + " - " + song.getName());
        } else {
            holder.playname.setText(song.getSinger());
        }
        if (from == 1) {
            holder.delete.setVisibility(View.INVISIBLE);
        }
        if (isplaying.get(position)) {
            holder.playing.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.INVISIBLE);
        } else {
            holder.playing.setVisibility(View.INVISIBLE);
            if (from == 0) {
                holder.delete.setVisibility(View.VISIBLE);
            }
        }
        holder.delete.setOnClickListener(this);
        holder.delete.setTag(position);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        mycallback.onClick(v);
    }

    public static class ViewHolder {
        ImageView playing, delete;
        TextView playname;
    }
}
