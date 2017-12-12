package com.merpyzf.httpcoreserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.merpyzf.httpcoreserver.http.HttpRequestListener;
import com.merpyzf.httpcoreserver.util.LogUtil;

/**
 * Created by wangke on 2017/12/1.
 * web服务
 */

public class WebService extends Service {

    private static final String TAG = WebService.class.getName();
    private HttpRequestListener mHttpRequestListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "onCreate方法执行了");
        mHttpRequestListener = new HttpRequestListener();
        mHttpRequestListener.start();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy方法执行了");

        if(mHttpRequestListener!=null){

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHttpRequestListener.destroy();
                }
            }).start();

        }
    }
}
