package com.merpyzf.transfermanager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.util.NetworkUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by wangke on 2017/12/3.
 */

public class PeerCommunicate extends Thread {


    private DatagramSocket mUdpSocket;
    private boolean isLoop = true;
    private Context mContext = null;
    private Handler mHandler = null;
    private static final String TAG = PeerCommunicate.class.getName();


    public PeerCommunicate(Context context, Handler handler) {

        mContext = context;
        mHandler = handler;
        init();
    }


    private void init() {
        try {
            // 初始化发送端所用socket
            mUdpSocket = new DatagramSocket(null);
            mUdpSocket.setReuseAddress(true);
            mUdpSocket.bind(new InetSocketAddress(Constant.PORT));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    // 监听UDP消息
    @Override
    public void run() {
        try {
            if (mUdpSocket != null) {
                while (isLoop) {
                    byte[] buffer = new byte[Constant.BUFFER_LENGTH];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, 0, Constant.BUFFER_LENGTH);
                    Log.i("wk", "开始监听UDP消息，阻塞。。。。。");
                    mUdpSocket.receive(receivePacket);

                    if (receivePacket.getLength() == 0) {
                        continue;
                    }

                    int port = receivePacket.getPort();
                    String hostAddress = receivePacket.getAddress().getHostAddress();

                    String receiveMsg = new String(buffer, 0, buffer.length, "utf-8");
                    Log.i(TAG, "接收的udp消息-->" + receiveMsg);



                    if (!NetworkUtil.isLocal(hostAddress)) {

                        SignMessage signMessage = SignMessage.decodeProtocol(receiveMsg);

                        Message message = Message.obtain();

                        message.obj = signMessage;

                        mHandler.sendMessage(message);

                    } else {
                        Log.i("wk", "收到的消息来自本机");
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            isLoop = false;
        } finally {


        }


    }

    /**
     * 发送UDP数据
     *
     * @param msg
     */
    public void sendUdpData(String msg, InetAddress dest, int port) {

        Log.i("wk", "发送UDP消息");

        try {

            if (mUdpSocket != null) {

                byte[] buffer = msg.getBytes(Constant.S_CHARSET);
                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, dest, port);
                mUdpSocket.send(sendPacket);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送广播信息
     */
    public void sendBroadcast(SignMessage signMessage) {
        //向全网发送探测信息
        InetAddress broadcastAddress = null;
        try {
            broadcastAddress = NetworkUtil.getBroadcastAddress(mContext);
            Log.i("wk", "组播地址-->" + broadcastAddress.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String sinMsg = signMessage.convertProtocolStr();
        Log.i("wk", sinMsg);
        sendUdpData(sinMsg, broadcastAddress, Constant.PORT);
    }



    /**
     * 释放资源
     */
    public void release() {

        if (mUdpSocket != null) {
            isLoop = false;
            mUdpSocket.close();

        }


    }

}
