package com.merpyzf.transfermanager.receive;

import com.merpyzf.transfermanager.entity.FileInfo;

/**
 * Created by wangke on 2017/12/22.
 */

public interface ReceiveTask {

    /**
     * 资源的初始化
     */
    void init();

    /**
     * 接收待接收文件列表
     */
    void receiveTransferFileList();

    /**
     * 解析文件头信息
     */
    FileInfo parseHeader();

    /**
     * 读取文件
     */
    void readBody(FileInfo fileInfo);

    /**
     * 释放资源
     */
    void release();


}
