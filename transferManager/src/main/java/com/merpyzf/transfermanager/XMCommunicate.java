package com.merpyzf.transfermanager;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ContentHandler;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by wangke on 2017/12/3.
 */

public class XMCommunicate {


    private DatagramSocket mUdpSocket;
    private int bufferLength = 8192;
    private boolean isLoop = true;
    private static final String TAG = XMCommunicate.class.getName();


    public XMCommunicate() {

        init();
    }


    private void init() {

        try {
            mUdpSocket = new DatagramSocket(null);
            mUdpSocket.setReuseAddress(true);
            mUdpSocket.bind(new InetSocketAddress(8900));


        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于向另一端发动消息
     *
     * @param msg
     */
    public synchronized void sendUdpMsg(String msg) {
        try {

            if (mUdpSocket != null) {

                byte[] buffer = msg.getBytes("utf-8");

                InetAddress destination = InetAddress.getByName("255.255.255.255");

                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, destination, 8900);

                Log.i(TAG, "向全网发送广播");
                //向全网发送广播
                mUdpSocket.send(sendPacket);

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 用于接收另一端发回的消息
     */
    public synchronized void receiveUdpMsg(final Activity context) {

        try {

            if (mUdpSocket != null) {
                while (isLoop) {
                    byte[] buffer = new byte[bufferLength];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, 0, bufferLength);
                    Log.i(TAG, "阻塞等待接收消息");
                    mUdpSocket.receive(receivePacket);
                    final String receiveMsg = new String(buffer, 0, buffer.length, "utf-8");
                    Log.i(TAG, "接收的udp消息-->" + receiveMsg);
                    String hostAddress = receivePacket.getAddress().getHostAddress();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(context, receiveMsg, Toast.LENGTH_SHORT).show();
                        }
                    });

                    byte[] replyContent = "mac - 172.28.67.83(我是接收端)".getBytes();

                    InetAddress dest = InetAddress.getByName(hostAddress);

                    mUdpSocket.send(new DatagramPacket(replyContent, 0, replyContent.length, dest, 8900));


                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            mUdpSocket.close();
        }


    }


}
