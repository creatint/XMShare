package com.merpyzf.xmshare.util.timer;

import android.os.Handler;

/**
 * Created by wangke on 2017/11/20.
 * 一个通用的计时器
 *
 */

public class OSTimer implements Runnable {

    private Handler mHandler = null;
    // 计时间隔的时间
    private int mInterval = 2;
    private TimeOut mTimeOut = null;
    private Boolean isCancel = false;

    // 是否循环执行
    private Boolean isCycle = false;

    public OSTimer(Handler handler, int interval, TimeOut timeOut) {

        if (handler == null) {

            mHandler = new Handler();

        } else {

            this.mHandler = mHandler;
        }

        this.mInterval = interval*1000;
        this.mTimeOut = timeOut;

    }

    /**
     * 停止计时
     */
    public void stop(){

        if(!isCancel){
            isCancel = true;
        }

    }

    /**
     * 设置循环计时
     */
    public void setCycle(){

        if(!isCycle){

            isCycle = true;
        }

    }

    public void start() {

        mHandler.postDelayed(this, mInterval);

    }


    @Override
    public void run() {

        if(!isCancel){

            if(mTimeOut!=null) {

                mTimeOut.completed();

            }
        }

        // 循环计时
        if(isCycle){
            start();
        }


    }
}
