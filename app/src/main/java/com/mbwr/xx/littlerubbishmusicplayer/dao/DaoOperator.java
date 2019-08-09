package com.mbwr.xx.littlerubbishmusicplayer.dao;

import android.database.Cursor;

import com.mbwr.xx.littlerubbishmusicplayer.model.Song;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class DaoOperator {
    public static List<Song> getSongsByAlbumId(long albumId) {
        List<Song> songList = new ArrayList<>();
        Cursor cursor = LitePal.findBySQL("select s.id " +
                "FROM song s " +
                "left join album_song t on s.id = t.song_id " +
                "where t.album_id = " + albumId);
        if (cursor.moveToFirst()) {
            do {
                songList.add(LitePal.find(Song.class, cursor.getLong(cursor.getColumnIndex("id"))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songList;
    }
}
