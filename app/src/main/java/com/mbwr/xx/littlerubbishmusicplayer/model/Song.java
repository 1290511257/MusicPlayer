package com.mbwr.xx.littlerubbishmusicplayer.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * @author xuxiong
 * @time 8/1/19  9:30 PM
 * @describe 音乐实体类
 */
public class Song extends LitePalSupport {

    @Column(nullable = false)
    private String name;

    private String singer;

    private long size;

    private List<Album> albums;

    @Column(defaultValue = "1")
    private boolean isAvailable;

    private String filePath;

    private String url;

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

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Song(){}

    public Song(String name, String singer, long size, List<Album> albums, String filePath, String url) {
        this.name = name;
        this.singer = singer;
        this.size = size;
        this.albums = albums;
        this.filePath = filePath;
        this.url = url;
        this.isAvailable = true;
    }
}
