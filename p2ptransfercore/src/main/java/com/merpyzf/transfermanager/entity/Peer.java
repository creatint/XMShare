package com.merpyzf.transfermanager.entity;

/**
 * Created by wangke on 2017/12/13.
 * 描述设备的对象
 */

public class Peer {

    // 昵称
    private String nickName;
    // 主机地址
    private String hostAddress;

    private String ssid;


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


    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        return hostAddress.equals(peer.hostAddress);
    }

    @Override
    public int hashCode() {
        return hostAddress.hashCode();
    }
}
