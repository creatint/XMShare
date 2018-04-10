package com.merpyzf.transfermanager;


import android.os.Handler;
import android.os.Message;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.interfaces.TransferObserver;
import com.merpyzf.transfermanager.receive.ReceiverManager;

import java.util.List;

/**
 * Created by wangke on 2018/1/22.
 */

public class P2pTransferHandler extends Handler {

    private List<TransferObserver> mTransferObserverLists;
    private ReceiverManager.TransferFileListListener mTransferFileListListener;


    public P2pTransferHandler(List<TransferObserver> transferObserverLists) {
        this.mTransferObserverLists = transferObserverLists;
    }

    public void setTransferFileListListener(ReceiverManager.TransferFileListListener transferFileListListener) {
        this.mTransferFileListListener =transferFileListListener;
    }

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
                    mTransferFileListListener.onReceiveCompleted(mReceiveFileList);

                }

                break;


            /**
             * 传输中
             */
            case Constant.TransferStatus.TRANSFING:

                FileInfo fileInfo = (FileInfo) msg.obj;


                for (TransferObserver transferObserver : mTransferObserverLists) {
                    transferObserver.onTransferProgress(fileInfo);

                }

                break;
            /**
             * 传输成功
             */
            case Constant.TransferStatus.TRANSFER_SUCCESS:


                FileInfo fileInfo1 = (FileInfo) msg.obj;

                for(int i=0;i<mTransferObserverLists.size();i++){
                    mTransferObserverLists.get(i).onTransferStatus(fileInfo1);



                }


                break;

            /**
             * 传输失败
             */
            case Constant.TransferStatus.TRANSFER_FAILED:


                break;

            default:
                break;
        }

    }
}
