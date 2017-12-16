package com.merpyzf.transfermanager.entity;

/**
 * Created by wangke on 2017/12/13.
 * 描述设备的对象
 */

public class Peer {

    private String nickName;
    private String hostAddress;

    public Peer() {
    }

    public Peer(String nickName, String hostAddress) {
        this.nickName = nickName;
        this.hostAddress = hostAddress;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
}
