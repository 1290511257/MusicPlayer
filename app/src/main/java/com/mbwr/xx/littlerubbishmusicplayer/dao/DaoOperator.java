package com.mbwr.xx.littlerubbishmusicplayer.dao;

import android.database.Cursor;

import com.mbwr.xx.littlerubbishmusicplayer.model.Album;
import com.mbwr.xx.littlerubbishmusicplayer.model.Song;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class DaoOperator {

    public Album getAlbumById(long albumId) {
        Album album = LitePal.find(Album.class, albumId);
        List<Song> songList = new ArrayList<>();
        Cursor cursor = LitePal.findBySQL("select s.id " +
                "FROM song s " +
                "left join album_song t on s.id = t.song_id " +
                "where t.album_id = " + albumId);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                Song song = LitePal.find(Song.class, id);
                songList.add(song);
            } while (cursor.moveToNext());
        }
        album.setSongs(songList);
        return album;
    }
}
