package com.merpyzf.transfermanager.p2pinterface;

import com.merpyzf.transfermanager.entity.Peer;

/**
 * Created by wangke on 2017/12/13.
 * 搜寻局域网内设备的事件回调
 */
public interface LocalDevice_Callback {
    void onFoundDevice(Peer peer);
}
