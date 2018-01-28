package com.merpyzf.transfermanager.interfaces;

import com.merpyzf.transfermanager.entity.Peer;

/**
 * Created by wangke on 2018/1/28.
 */

public interface PeerTransferBreakCallBack {

    /**
     * 中断传输
     */
    void onTransferBreak(Peer peer);
}
