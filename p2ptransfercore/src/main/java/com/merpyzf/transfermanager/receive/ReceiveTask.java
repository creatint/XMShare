package com.merpyzf.transfermanager.receive;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by wangke on 2017/12/22.
 */

public class ReceiveTask implements Runnable, IReceiveTask {

    private Socket mSocket;

    public ReceiveTask(Socket socket) {
        this.mSocket = socket;
    }

    @Override
    public void run() {


        try {
            OutputStream os = mSocket.getOutputStream();

            os.write("hello".getBytes());

            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void init() {

    }

    @Override
    public void parseHeader() {

    }

    @Override
    public void parseBody() {

    }

    @Override
    public void release() {

    }
}
