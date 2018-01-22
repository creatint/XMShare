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
    private SenderTask mSenderTask;

    public SenderManager(Context context) {
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        this.mContext = context;
    }


    public void send(List<FileInfo> filelist) {

        mSenderTask = new SenderTask(mContext, filelist);

        mSingleThreadPool.execute(mSenderTask);

    }

    /**
     * 传输结束时进行资源的释放
     */
    public void release() {
        mSenderTask.release();
    }
}
