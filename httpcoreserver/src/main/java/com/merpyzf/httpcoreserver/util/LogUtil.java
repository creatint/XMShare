package com.merpyzf.httpcoreserver.util;

import android.util.Log;

/**
 * Created by wangke on 2017/12/2.
 */

public class LogUtil {


    private static boolean isShow = true;


    public static void i(String tag, String info) {

        if (isShow) {

            Log.i(tag, info);

        }

    }


}
