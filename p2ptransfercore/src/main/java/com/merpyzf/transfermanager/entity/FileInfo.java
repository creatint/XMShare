package com.merpyzf.transfermanager.entity;

/**
 * Created by wangke on 2017/12/23.
 */

public class FileInfo {

    /**
     * 应用类型(APK)
     */
    public static final int FILE_TYPE_APP = 1;

    /**
     * 图片类型
     */
    public static final int FILE_TYPE_IMAGE = 2;

    /**
     * 音乐类型
     */
    public static final int FILE_TYPE_MUSIC = 3;

    /**
     * 视频类型
     */
    public static final int FILE_TYPE_VIDEO = 4;


    // 文件id
    private String id;
    // 文件名
    private String name;
    // 文件路径
    private String path;
    // 文件类型
    private String type;
    // 文件的后缀名
    private String suffix;
    // 文件大小
    private long size;


    public FileInfo() {
    }

    public FileInfo(String name, String path, String type, long size, String suffix) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
        this.suffix = suffix;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}