package com.mbwr.xx.littlerubbishmusicplayer.model;

import org.litepal.crud.LitePalSupport;

/**
 * Created by zhpan on 2018/1/22.
 */

public class Device extends LitePalSupport{
    public Device(String name, int state) {
        this.name = name;
        this.state = state;
    }

    private String name;
    private int state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
