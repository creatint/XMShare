package com.merpyzf.transfermanager.receive;


import android.util.Log;

import com.merpyzf.transfermanager.constant.Constant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke on 2017/12/12.
 */

public class Receiver {

    private ExecutorService mThreadPool;
    private ServerSocket serverSocket;

    public Receiver() {

        mThreadPool = Executors.newCachedThreadPool();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Constant.SOCKET_PORT);

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(inetSocketAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 监听设备连接开始准备接收文件
     */
    public void startReceive() {


        new Thread(new Runnable() {
            @Override
            public void run() {


                try {

                    while (true) {


                        Socket clientSocket = serverSocket.accept();
                        Log.i("wk","有设备连接"+clientSocket.getInetAddress().getHostAddress());
                        mThreadPool.execute(new ReceiveTask(clientSocket));


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


}
