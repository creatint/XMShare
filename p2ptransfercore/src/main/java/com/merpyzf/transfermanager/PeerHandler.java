package com.merpyzf.transfermanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;

/**
 * Created by wangke on 2017/12/16.
 * 接收从子线程转发来的UDP消息，并进行消息内容的处理和回调分发
 */
public class PeerHandler extends Handler {
    private Context mContext = null;
    private PeerCommunicate mPeerCommunicate;
    private PeerCommunCallback mPeerCommunCallback = null;

    public PeerHandler(Context mContext, PeerCommunCallback peerCommunCallback) {
        this.mContext = mContext;
        this.mPeerCommunCallback = peerCommunCallback;
    }

    public PeerHandler(Looper looper) {
        super(looper);
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

        Peer peer = new Peer();
        peer.setHostAddress(signMessage.getHostAddress());
        peer.setNickName(signMessage.getNickName());

        // 将事件的处理拆分到不同的类中进行处理
        // 1. PeerManager处理设备的上线下线
        // 2. sendManager处理文件的发送
        // 3. receiveManager处理文件的接收

        switch (cmd) {


            // 设备上线
            case Constant.cmd.ON_LINE:

                if (mPeerCommunCallback != null) {

                    mPeerCommunCallback.onDeviceOnLine(peer);
                }

                break;

            // 设备下线
            case Constant.cmd.OFF_LINE:

                if (mPeerCommunCallback != null) {

                    mPeerCommunCallback.onDeviceOffLine(peer);
                }


                break;


            case Constant.cmd.REQUEST_CONN:


                break;


        }


    }


}



