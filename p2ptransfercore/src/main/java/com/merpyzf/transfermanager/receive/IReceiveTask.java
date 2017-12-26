package com.merpyzf.transfermanager.receive;

/**
 * Created by wangke on 2017/12/22.
 */

public interface IReceiveTask {

    void init();
    void parseHeader();
    void parseBody();
    void release();




}
