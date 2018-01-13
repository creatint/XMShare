package com.merpyzf.transfermanager.p2pinterface;

/**
 * Created by wangke on 2017/12/22.
 */

public interface ReceiveTaskable {

    void init();
    void parseHeader();
    void parseBody();
    void release();




}
