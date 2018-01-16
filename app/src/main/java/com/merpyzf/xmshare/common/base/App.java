package com.merpyzf.xmshare.common.base;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.merpyzf.transfermanager.entity.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangke on 2018/1/16.
 */

public class App extends Application {


    public static Context AppContext;

    /**
     * 待发送的文件集合
     */
    public static List<FileInfo> mSendFileList;


    @Override
    public void onCreate() {
        super.onCreate();
        AppContext = getApplicationContext();
        mSendFileList = new ArrayList<>();
        Log.i("w2k", "application的 onCreate方法执行了");
    }


    /**
     * 从文件集合中添加一个待发送的文件
     *
     * @param fileInfo
     */
    public static void addSendFile(FileInfo fileInfo) {

        if (!mSendFileList.contains(fileInfo)) {
            mSendFileList.add(fileInfo);
        }
    }

    /**
     * 从文件集合中移除一个待发送的文件
     *
     * @param fileInfo
     */
    public static void removeSendFile(FileInfo fileInfo) {

        if (mSendFileList.contains(fileInfo)) {
            mSendFileList.remove(fileInfo);
        }
    }

    /**
     * 返回待发送的文件集合
     *
     * @return
     */
    public static List<FileInfo> getSendFileList() {

        return mSendFileList;
    }


}
