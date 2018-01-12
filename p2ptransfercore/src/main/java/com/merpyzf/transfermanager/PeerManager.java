package com.merpyzf.transfermanager;

import android.content.Context;
import android.util.Log;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.transfermanager.util.NetworkUtil;
import com.merpyzf.transfermanager.util.timer.OSTimer;
import com.merpyzf.transfermanager.util.timer.Timeout;

import java.net.InetAddress;
import java.util.Timer;

/**
 * Created by wangke on 2017/12/17.
 */

public class PeerManager {

    private Context mContext = null;
    private PeerHandler mPeerHandler;
    private PeerCommunCallback mPeerCallback = null;
    private OSTimer mOsTimer;
    private PeerCommunicate mPeerCommunicate;
    private String nickName;
    private OSTimer mOnLineTimer;
    private Timer mTimer;
    private boolean isStop = false;


    public PeerManager(Context context, String nickName,PeerCommunCallback peerCallback) {
        this.mContext = context;
        this.nickName = nickName;
        // 创建Handler用于接收的UDP消息处理
        this.mPeerHandler = new PeerHandler(mContext, peerCallback);
        this.mPeerCommunicate = new PeerCommunicate(mContext, mPeerHandler);
    }

    /**
     * 发送设备上线广播
     */
    public OSTimer sendOnLineBroadcast(boolean isCycle) {

        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String name = Thread.currentThread().getName();
                        SignMessage signMessage = new SignMessage();
                        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
                        signMessage.setMsgContent("ON_LINE");
                        signMessage.setCmd(Constant.cmd.ON_LINE);
                        signMessage.setNickName(nickName);
                        Log.i("w2k", "发送上线广播");
                        sendBroadcastMsg(signMessage);

                    }
                }).start();
            }
        };

        timeout.onTimeOut();
        //发送两个广播消息
        OSTimer osTimer = new OSTimer(null, timeout, 100, isCycle);
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

                        Log.i("wk", "发送设备离线广播");
                        String name = Thread.currentThread().getName();

//                        Log.i("wk", "所在线程-->" + name);

                        SignMessage signMessage = new SignMessage();

                        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
                        signMessage.setMsgContent("OFF_LINE");
                        signMessage.setCmd(Constant.cmd.OFF_LINE);

                        signMessage.setNickName("merpyzf");
                        sendBroadcastMsg(signMessage);

                    }
                }).start();
            }
        };

        timeout.onTimeOut();
        //发送两个广播消息
        new OSTimer(null, timeout, 50, false).start();
        new OSTimer(null, timeout, 100, false).start();

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
    public void stopUdpServer() {

        mPeerCommunicate.release();


    }


    /**
     * 停止发送上线广播，终止循环发送
     */
    public void stopSendOnlineBroadcast() {

        isStop = true;
        if (mTimer != null) {
            Log.i("w2k", "停止继续发送上线广播");
            mTimer.cancel();
        }
    }


    /**
     * 给局域网内的其他设备发送UDP
     */
    public void send2Peer(String msg, InetAddress dest, int port) {
        mPeerCommunicate.sendUdpData(msg, dest, port);
    }


    /**
     * 发送组播消息
     *
     * @param signMessage
     */
    public void sendBroadcastMsg(SignMessage signMessage) {

        mPeerCommunicate.sendBroadcast(signMessage);
    }
}

