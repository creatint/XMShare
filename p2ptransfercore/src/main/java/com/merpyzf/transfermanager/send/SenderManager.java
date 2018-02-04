package com.merpyzf.transfermanager.send;

import android.content.Context;
import android.util.Log;

import com.merpyzf.transfermanager.P2pTransferHandler;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.interfaces.TransferObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke on 2018/1/17.
 */

public class SenderManager {

    private ExecutorService mSingleThreadPool;
    private Context mContext;
    private static SenderManager mSenderManager;
    private SenderTask mSenderTask;
    private P2pTransferHandler mP2pTransferHandler;
    // 观察者集合
    private List<TransferObserver> mTransferObserverLists;
    private String TAG = SenderManager.class.getSimpleName();


    public static SenderManager getInstance(Context context) {

        if (mSenderManager == null) {

            synchronized (Object.class) {

                if (mSenderManager == null) {

                    mSenderManager = new SenderManager(context);

                }

            }

        }
        return mSenderManager;
    }


    private SenderManager(Context context) {
        mTransferObserverLists = new ArrayList<>();
        mP2pTransferHandler = new P2pTransferHandler(mTransferObserverLists);
        mSingleThreadPool = Executors.newCachedThreadPool();
        this.mContext = context;
    }


    public void register(TransferObserver transferObserver) {

        mTransferObserverLists.add(transferObserver);

    }


    public void unRegister(TransferObserver transferObserver) {



        if (mTransferObserverLists.contains(transferObserver)) {
            mTransferObserverLists.remove(transferObserver);
            Log.i(TAG, "unRegister被调用");
        }

    }

    /**
     * 发送文件， 异步的过程
     *
     * @param filelist
     */
    public void send(String destAddress, List<FileInfo> filelist) {

        mSenderTask = new SenderTask(mContext, destAddress, filelist, mP2pTransferHandler);
        mSingleThreadPool.execute(mSenderTask);

    }

    /**
     * 传输结束时进行资源的释放
     */
    public void release() {
        mSenderTask.release();
    }
}
