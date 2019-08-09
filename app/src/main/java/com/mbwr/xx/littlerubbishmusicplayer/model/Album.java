package com.mbwr.xx.littlerubbishmusicplayer.model;

import com.mbwr.xx.littlerubbishmusicplayer.dao.DaoOperator;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 *  @author xuxiong
 *  @time 8/1/19  9:29 PM
 *  @describe 这里的album指的是本地音乐歌单,并不是歌手专辑
 */
public class Album extends LitePalSupport {

    @Column(nullable = false,unique = true)
    private String name;

    @Column(defaultValue = "1")
    private boolean isAvailable;

    private List<Song> songs;

    public long getId() {
        return super.getBaseObjId();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return DaoOperator.getSongsByAlbumId(getId());
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Album(){}

    public Album(String name, List<Song> songs) {
        this.name = name;
        this.songs = songs;
        this.isAvailable = true;
    }
}
