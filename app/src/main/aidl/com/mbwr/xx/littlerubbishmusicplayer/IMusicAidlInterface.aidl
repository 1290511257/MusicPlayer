// IMusicAidlInterface.aidl
package com.mbwr.xx.littlerubbishmusicplayer;

// Declare any non-default types here with import statements

interface IMusicAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    long getAlbumId();
    long getSongId();
    long getPlayMode();

    void setPlayInfo(long albumId,long songId,long playMode);
}
