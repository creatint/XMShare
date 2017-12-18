package com.merpyzf.transfermanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.SignMessage;

import java.net.InetAddress;

/**
 * Created by wangke on 2017/12/16.
 * 包含UDP消息的收发，和UDP消息的处理
 */
public class PeerHandler extends Handler {
    private Context mContext = null;
    private PeerCommunicate mPeerCommunicate;
    private PeerManager mPeerManager = null;

    public PeerHandler(Context mContext, PeerManager peerManager) {
        this.mContext = mContext;
        this.mPeerManager = peerManager;
        init(mContext);
    }

    public PeerHandler(Looper looper) {
        super(looper);
    }

    private void init(Context context) {

        mContext = context;
        mPeerCommunicate = new PeerCommunicate(mContext, this);

    }

    public PeerCommunicate getmPeerCommunicate() {
        return mPeerCommunicate;
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





    /**
     * 获取接收到的udp消息
     * <p>
     * 1.我在线中可连接 (将可连接的设备显示在界面上)
     * 2.请求连接 ()
     * 3.回复可以连接
     *
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        SignMessage signMessage = (SignMessage) msg.obj;
        int cmd = signMessage.getCmd();

        // 将事件的处理拆分到不同的类中进行处理
        // 1. PeerManager处理设备的上线下线
        // 2. sendManager处理文件的发送
        // 3. receiveManager处理文件的接收
        switch (cmd) {

            // 设备上线
            case Constant.cmd.ON_LINE:

                mPeerManager.dispatchMSG(signMessage);
                break;

            // 设备下线
            case Constant.cmd.OFF_LINE:
                mPeerManager.dispatchMSG(signMessage);

                break;


            case Constant.cmd.REQUEST_CONN:


                break;


        }


    }
}

