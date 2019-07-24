package com.mbwr.xx.littlerubbishmusicplayer.inter;

import android.content.Intent;

import com.mbwr.xx.littlerubbishmusicplayer.model.Album;

public interface MediaController {

    void CallStop();

    void CallPause();

    void CallPlay();

    void CallPlay(int position);

    void CallResume();

    void CallLastMusic();

    void CallNextMusic();

    void UpdateSeekBar(int position);

    void UpdateAlbum(Album album);

    void UpdatePlayMode(int i);
}
