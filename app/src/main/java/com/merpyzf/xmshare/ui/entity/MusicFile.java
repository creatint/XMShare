package com.merpyzf.xmshare.ui.entity;

import com.merpyzf.transfermanager.entity.FileInfo;

/**
 * Created by wangke on 2017/12/23.
 */

public class MusicFile extends FileInfo {

    // 封面id
    private long albumId;
    // 作者
    private String artist;
    // 时长
    private long duration;



    public MusicFile() {
    }

    public MusicFile(String name, String path, String type, long size, long albumId, String artist, long duration) {
        super(name, path, type, size);
        this.albumId = albumId;
        this.artist = artist;
        this.duration = duration;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
