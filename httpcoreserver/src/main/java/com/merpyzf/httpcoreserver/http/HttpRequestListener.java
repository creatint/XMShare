package com.merpyzf.httpcoreserver.http;

import com.merpyzf.httpcoreserver.constant.Constant;
import com.merpyzf.httpcoreserver.handle.FileBrowserHandler;
import com.merpyzf.httpcoreserver.handle.RequestHandleTask;
import com.merpyzf.httpcoreserver.util.LogUtil;

import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangke on 2017/12/1.
 */

public class HttpRequestListener extends Thread {

    private static final String TAG = HttpRequestListener.class.getName();
    private ServerSocket mServerSocket;
    private static boolean isLoop = true;
    private HttpService mHttpService;
    private BasicHttpParams mHttpParams;
    private ExecutorService mExecutorPool = Executors.newCachedThreadPool();

    @Override
    public void run() {
        initServer();
        startListen();
    }


    public void initServer(){


        try {
            // 1.创建ServerSocket用来接收客户端的请求
            mServerSocket = new ServerSocket();
            // 这个方法的作用？
            mServerSocket.setReuseAddress(true);
            mServerSocket.bind(new InetSocketAddress(Constant.PORT));

            // 2.添加HTTP协议拦截器，给response添加响应头信息
            BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
            httpProcessor.addInterceptor(new ResponseDate());
            httpProcessor.addInterceptor(new ResponseServer());
            httpProcessor.addInterceptor(new ResponseContent());
            httpProcessor.addInterceptor(new ResponseConnControl());

            // 3.HttpParams初始化http信息
            mHttpParams = new BasicHttpParams();
            mHttpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8*1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WebServer/1.1");

            // 4.添加接口名称
            HttpRequestHandlerRegistry handlerRegistry = new HttpRequestHandlerRegistry();
            handlerRegistry.register("*", new FileBrowserHandler());

            // 5.创建Http服务

            mHttpService = new HttpService(httpProcessor, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
            mHttpService.setParams(mHttpParams);
            mHttpService.setHandlerResolver(handlerRegistry);


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 监听客户端的连接
     */
    public void startListen(){

        while (isLoop){

            if(mServerSocket==null){

                LogUtil.i(TAG, "mServerSocket为null");
                return;
            }
            if(!mServerSocket.isClosed()){
                // 阻塞等待客户端的接入
                try {


                    LogUtil.i(TAG, "等待客户端接入....");
                    Socket socketClient = mServerSocket.accept();

                    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

                    serverConnection.bind(socketClient,mHttpParams);

                    // 接入客户端绑定的任务放入创建的线程池中处理
                    mExecutorPool.execute(new RequestHandleTask(mHttpService,serverConnection));


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }



        }



    }


}
