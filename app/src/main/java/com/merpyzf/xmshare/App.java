package com.merpyzf.xmshare;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.litesuits.orm.LiteOrm;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.util.Md5Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangke on 2018/1/16.
 */

public class App extends Application {


    private static Context AppContext;

    /**
     * 待发送的文件集合
     */
    private static List<FileInfo> mSendFileList;
    private static ExecutorService mSingleThreadPool;
    private static LiteOrm mSingleLiteOrm;
    private static String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext = getApplicationContext();
        mSendFileList = new ArrayList<>();
        mSingleThreadPool = Executors.newSingleThreadExecutor();
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

            Observable.just(fileInfo)
                    .flatMap((Function<FileInfo, ObservableSource<File>>) fileInfo1 -> Observable.just(new File(fileInfo1.getPath())))
                    .subscribeOn(AndroidSchedulers.mainThread())// 指定被观察者执行所在的线程
                    .observeOn(Schedulers.io()) // 指定观察者执行所在的线程
                    .subscribe(file -> {
                        // 观察者
                        String md5 = Md5Utils.getMd5(file);
                        fileInfo.setMd5(md5);

                        Log.i(TAG, fileInfo.getName() + "-> \n" + md5);

                    });


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
            //
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

    /**
     * 重置待发送文件集合的状态
     */
    public static void resetSendFileList() {

        for (int i = 0; i < mSendFileList.size(); i++) {
            FileInfo fileInfo = mSendFileList.get(i);
            fileInfo.reset();
        }
    }


    public static ExecutorService getSingleThreadPool() {
        return mSingleThreadPool;
    }

    public static LiteOrm getSingleLiteOrm() {

        if (mSingleLiteOrm == null) {
            synchronized (Object.class) {
                if (mSingleLiteOrm == null) {

                    mSingleLiteOrm = LiteOrm.newSingleInstance(AppContext, Constant.DB_NAME);
                    // 开启Debug
                    mSingleLiteOrm.setDebugged(true);
                }
            }
        }
        return mSingleLiteOrm;
    }


    public static Context getAppContext() {
        return AppContext;
    }
}
