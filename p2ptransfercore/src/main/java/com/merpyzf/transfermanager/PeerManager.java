package com.merpyzf.transfermanager;

import android.content.Context;
import android.util.Log;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.transfermanager.util.NetworkUtil;
import com.merpyzf.transfermanager.util.timer.OSTimer;
import com.merpyzf.transfermanager.util.timer.Timeout;

/**
 * Created by wangke on 2017/12/17.
 */

public class PeerManager {

    private Context mContext = null;
    private PeerHandler mPeerHandler;
    private PeerCommunCallback mPeerCallback = null;
    private OSTimer mOsTimer;
    private PeerCommunicate mPeerCommunicate;


    public PeerManager(Context context) {
        mContext = context;
        init();
    }


    private void init() {

        mPeerHandler = new PeerHandler(mContext, this);

        mPeerCommunicate = mPeerHandler.getmPeerCommunicate();

    }

    /**
     * @param signMessage
     */
    public void dispatchMSG(SignMessage signMessage) {

        int cmd = signMessage.getCmd();

        Peer peer = new Peer();
        peer.setHostAddress(signMessage.getHostAddress());
        peer.setNickName(signMessage.getNickName());

        switch (cmd) {

            // 设备上线
            case Constant.cmd.ON_LINE:

                if (mPeerCallback != null) {
                    mPeerCallback.onDeviceOnLine(peer);
                }
                break;
            // 设备下线
            case Constant.cmd.OFF_LINE:

                if (mPeerCallback != null) {
                    mPeerCallback.onDeviceOffLine(peer);
                }
                break;


            case Constant.cmd.REQUEST_CONN:

                break;

            default:

                break;

        }


    }


    /**
     * 发送设备上线广播
     */
    public OSTimer sendOnLineBroadcast() {


        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {


                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        Log.i("wk", "定时发送上线广播");
                        String name = Thread.currentThread().getName();

                        Log.i("wk", "所在线程-->" + name);

                        SignMessage signMessage = new SignMessage();

                        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
                        signMessage.setMsgContent("ON_LINE");
                        signMessage.setCmd(Constant.cmd.ON_LINE);
                        signMessage.setNickName("merpyzf");
                        mPeerHandler.sendBroadcastMsg(signMessage);

                    }
                }).start();

            }
        };

        timeout.onTimeOut();
        //发送两个广播消息
        OSTimer osTimer = new OSTimer(null, timeout, 250, true);
        osTimer.start();

        return osTimer;

    }


    /**
     * 发送设备下线广播
     */
    public void sendOffLineBroadcast() {


        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.i("wk", "定时发送下线广播");
                        String name = Thread.currentThread().getName();

                        Log.i("wk", "所在线程-->" + name);

                        SignMessage signMessage = new SignMessage();

                        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
                        signMessage.setMsgContent("OFF_LINE");
                        signMessage.setCmd(Constant.cmd.OFF_LINE);
                        signMessage.setNickName("merpyzf");
                        mPeerHandler.sendBroadcastMsg(signMessage);
                    }
                }).start();
            }
        };

        timeout.onTimeOut();
        //发送两个广播消息
        new OSTimer(null, timeout, 50, false).start();
        new OSTimer(null, timeout, 100, false).start();
        new OSTimer(null, timeout, 150, false).start();



    }

    /**
     * 开启广播监听
     */
    public void listenBroadcast() {
        if (mPeerCommunicate != null) {
            mPeerCommunicate.start();
        }
    }

    /**
     * 停止广播监听
     */
    public void stopListen() {

        mPeerCommunicate.release();


    }

    /**
     * 设置局域网内设备在线状态的监听
     *
     * @param mPeerCallback
     */
    public void setOnPeerCallback(PeerCommunCallback mPeerCallback) {
        this.mPeerCallback = mPeerCallback;
    }

}

