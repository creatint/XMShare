package com.merpyzf.httpcoreserver.handle;

import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpService;

import java.io.IOException;

/**
 * Created by wangke on 2017/12/1.
 */

public class RequestHandleTask extends Thread {

    private HttpService mHttpService = null;
    private HttpServerConnection mServerConn = null;


    public RequestHandleTask(HttpService httpService, HttpServerConnection serverConn) {
        this.mHttpService = httpService;
        this.mServerConn = serverConn;
    }

    @Override
    public void run() {

        BasicHttpContext httpContext = new BasicHttpContext();
        try {

            while (!Thread.interrupted() && mServerConn.isOpen()) {

                mHttpService.handleRequest(mServerConn, httpContext);

            }

        } catch (IOException e) {
            e.printStackTrace();
            // 中断线程
            interrupt();
        } catch (HttpException e) {
            e.printStackTrace();
            interrupt();
        }finally {
            try {
                // 当出现异常时或程序执行完毕后关闭连接
                mServerConn.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
