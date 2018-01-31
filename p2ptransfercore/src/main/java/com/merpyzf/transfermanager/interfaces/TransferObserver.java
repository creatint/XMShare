package com.merpyzf.transfermanager.interfaces;

import com.merpyzf.transfermanager.entity.FileInfo;

/**
 * Created by wangke on 2018/1/22.
 * 文件传输过程中的观察者的回调事件
 */

public interface TransferObserver {

    /**
     * 文件接收进度的回调
     */
    void onTransferProgress(FileInfo fileInfo);

    /**
     * 文件接收状态的回调
     */
    void onTransferStatus(FileInfo fileInfo);
}