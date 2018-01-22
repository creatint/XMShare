package com.merpyzf.transfermanager.receive;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.FileInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke on 2017/12/12.
 * 负责处理文件的接收
 */
public class ReceiverManager implements Runnable {

    private ServerSocket mServerSocket;
    private ExecutorService mSingleThreadPool;
    private Socket mSocketClient;
    private ReceiveHandler mReceiveHandler;
    private static ReceiverManager mReceiver;
    // 观察者集合
    private List<ReceiveObserver> mReceiveObserverLists;
    private TransferFileListListener mTransferFileListListener;

    private ReceiveTask mReceiveTask;
    private boolean isStop = false;

    /**
     * 单例获取实例
     *
     * @return
     */
    public static ReceiverManager getInstance() {

        if (mReceiver == null) {
            synchronized (Object.class) {
                if (mReceiver == null) {

                    mReceiver = new ReceiverManager();

                }
            }
        }
        return mReceiver;
    }


    private ReceiverManager() {

        try {
            mReceiveHandler = new ReceiveHandler();
            mReceiveObserverLists = new ArrayList<>();
            mServerSocket = new ServerSocket(Constant.SOCKET_PORT);
            mSingleThreadPool = Executors.newSingleThreadExecutor();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 注册一个观察者
     *
     * @param receiveObserver
     */

    public void register(ReceiveObserver receiveObserver) {

        if (!mReceiveObserverLists.contains(receiveObserver)) {
            mReceiveObserverLists.add(receiveObserver);
        }

    }


    /**
     * 接触注册一个观察者
     *
     * @param receiveObserver
     */
    public void unRegister(ReceiveObserver receiveObserver) {
        if (mReceiveObserverLists.contains(receiveObserver)) {
            mReceiveObserverLists.remove(receiveObserver);
        }
    }

    public void setOnTransferFileListListener(TransferFileListListener transferFileListListener) {
        this.mTransferFileListListener = transferFileListListener;
    }

    @Override
    public void run() {

        while (!isStop) {

            try {
                Log.i("w2k", "阻塞中,等待设备连接....");
                mSocketClient = mServerSocket.accept();
                Log.i("w2k", "有设备连接:" + mSocketClient.getInetAddress().getHostAddress());
                mReceiveTask = new ReceiveTask(mSocketClient, mReceiveHandler);
                mSingleThreadPool.execute(mReceiveTask);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    class ReceiveHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                /**
                 * 待接收文件列表传输完毕
                 */
                case Constant.TransferStatus.TRANSFER_FILE_LIST_SUCCESS:

                    List<FileInfo> mReceiveFileList = (List<FileInfo>) msg.obj;

                    if (mTransferFileListListener != null && mReceiveFileList.size() > 0) {
                        // 当接收到待传输文件列表时的回调
                        mTransferFileListListener.onReceiveListCompleted(mReceiveFileList);

                    }

                    break;


                /**
                 * 传输中
                 */
                case Constant.TransferStatus.TRANSFING:

                    FileInfo fileInfo = (FileInfo) msg.obj;

                    for (ReceiveObserver receiveObserver : mReceiveObserverLists) {
                        receiveObserver.onReceiveProgress(fileInfo);

                    }

                    break;
                /**
                 * 传输成功
                 */
                case Constant.TransferStatus.TRANSFER_SUCCESS:


                    FileInfo fileInfo1 = (FileInfo) msg.obj;
                    for (ReceiveObserver receiveObserver : mReceiveObserverLists) {
                        receiveObserver.onReceiveStatus(fileInfo1);

                    }
                    break;


                case Constant.TransferStatus.TRANSFER_FAILED:


                    break;

                default:
                    break;
            }


        }
    }

    /**
     * 传输文件列表监听接口
     */
    public interface TransferFileListListener {

        /**
         * 接收待传输文件列表完成的回调
         *
         * @param receiveFileList
         */
        void onReceiveListCompleted(List<FileInfo> receiveFileList);


    }

    public interface ReceiveObserver {

        /**
         * 文件接收进度的回调
         */
        void onReceiveProgress(FileInfo fileInfo);

        /**
         * 文件接收状态的回调
         */
        void onReceiveStatus(FileInfo fileInfo);
    }

    /**
     * 释放资源
     */
    public void release() {
        isStop = true;
        mReceiveTask.release();
    }
}
