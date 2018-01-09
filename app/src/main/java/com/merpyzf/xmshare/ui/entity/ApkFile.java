package com.merpyzf.xmshare.ui.entity;

import android.graphics.drawable.Drawable;

import com.merpyzf.transfermanager.entity.FileInfo;

/**
 * Created by wangke on 2017/12/24.
 * 应用
 */

public class ApkFile extends FileInfo {

    private Drawable aplDrawable;


    public ApkFile(String name, String path, String type, long size, Drawable aplDrawable) {
        super(name, path, type, size,"apk");
        this.aplDrawable = aplDrawable;
    }

    public ApkFile() {
    }


    public Drawable getApkDrawable() {
        return aplDrawable;
    }

    public void setApkDrawable(Drawable aplDrawable) {
        this.aplDrawable = aplDrawable;
    }
}
