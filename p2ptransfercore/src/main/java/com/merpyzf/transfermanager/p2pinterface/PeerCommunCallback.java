package com.merpyzf.transfermanager.p2pinterface;

import com.merpyzf.transfermanager.entity.Peer;

/**
 * Created by wangke on 2017/12/13.
 * 搜寻局域网内设备的事件回调
 */
public interface PeerCommunCallback {

    /**
     * 设备上线
     * @param peer
     */
    void onDeviceOnLine(Peer peer);

    /**
     * 设备离线
     * @param peer
     */
    void onDeviceOffLine(Peer peer);
}
