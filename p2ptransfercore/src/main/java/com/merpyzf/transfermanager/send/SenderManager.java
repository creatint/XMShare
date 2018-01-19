package com.merpyzf.transfermanager.send;

import android.content.Context;

import com.merpyzf.transfermanager.entity.FileInfo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke on 2018/1/17.
 */

public class SenderManager {


    private ExecutorService mSingleThreadPool;
    private Context mContext;

    public SenderManager(Context context) {
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        this.mContext = context;
    }


    public void send(List<FileInfo> filelist) {

        mSingleThreadPool.execute(new SenderTask(mContext,filelist));

    }
}
