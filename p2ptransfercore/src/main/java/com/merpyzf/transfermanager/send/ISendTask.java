package com.merpyzf.transfermanager.send;

import com.merpyzf.transfermanager.entity.FileInfo;

/**
 * Created by wangke on 2017/12/22.
 */

public interface ISendTask {

    /**
     * 资源的初始化
     */
    void init();

    /**
     * 预先发送待传输的文件列表
     */
    void sendTransferFileList();

    /**
     * 解析文件头信息
     */
    void sendHeader(FileInfo file);

    /**
     * 读取文件
     */
    void sendBody(FileInfo file);

    /**
     * 释放资源
     */
    void release();


}
