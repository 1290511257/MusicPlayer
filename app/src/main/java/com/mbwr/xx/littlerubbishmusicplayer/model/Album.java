package com.mbwr.xx.littlerubbishmusicplayer.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.List;

public class Album extends LitePalSupport {

    @Column(nullable = false,unique = true)
    private String name;

    private List<Song> songs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Album(){}

    public Album(String name, List<Song> songs) {
        this.name = name;
        this.songs = songs;
    }
}
