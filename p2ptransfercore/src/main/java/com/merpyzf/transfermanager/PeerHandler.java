package com.merpyzf.transfermanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.util.NetworkUtil;

import java.net.InetAddress;

/**
 * Created by wangke on 2017/12/16.
 * 包含UDP消息的收发，和UDP消息的处理
 */
public class PeerHandler extends Handler {
    private Context mContext = null;
    private PeerCommunicate mPeerCommunicate;

    public PeerHandler(Looper looper) {
        super(looper);
    }

    private void init(Context context) {

        mContext = context;
        mPeerCommunicate = new PeerCommunicate(mContext, this);
    }

    /**
     * 给局域网内的其他设备发送UDP
     */
    public void send2Peer(String msg, InetAddress dest, int port){
        mPeerCommunicate.sendUdpData(msg, dest, port);
    }

    /**
     * 发送设备上线广播
     */
    public void sendOnLineBroadcast(){

        SignMessage signMessage = new SignMessage();

        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
        signMessage.setMsgContent("ON_LINE");
        signMessage.setCmd(Constant.cmd.ON_LINE);
        signMessage.setNickName("merpyzf");

        mPeerCommunicate.sendBroadcast(signMessage);
    }

    /**
     * 发送设备下线广播
     */
    public void sendOffLineBroadcast(){
        SignMessage signMessage = new SignMessage();
        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
        signMessage.setMsgContent("OFF_LINE");
        signMessage.setCmd(Constant.cmd.OFF_LINE);
        signMessage.setNickName("merpyzf");
        mPeerCommunicate.sendBroadcast(signMessage);

    }

    /**
     * 获取接收到的udp消息
     *
     * 1.我在线中可连接 (将可连接的设备显示在界面上)
     * 2.请求连接 ()
     * 3.回复可以连接
     *
     *
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        SignMessage signMessage = (SignMessage) msg.obj;
        int cmd = signMessage.getCmd();

        switch (cmd){

            // 设备上线
            case  Constant.cmd.ON_LINE:

                Log.i("wk", "有设备上线了");
                Log.i("wk", signMessage.getMsgContent());




                break;

            // 设备下线
            case Constant.cmd.OFF_LINE:

                Log.i("wk", "有设备下线了");
                Log.i("wk", signMessage.getMsgContent());


                break;


            case Constant.cmd.REQUEST_CONN:



                break;



        }



    }
}

