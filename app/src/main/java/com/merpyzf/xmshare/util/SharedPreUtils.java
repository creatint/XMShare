package com.merpyzf.xmshare.util;

import android.content.Context;

/**
 * Created by wangke on 2018/1/11.
 */

public class SharedPreUtils {


    public static void putString(Context context, String spName, String key, String value) {
        context.getSharedPreferences(spName, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .commit();
    }


    public static String getString(Context context, String spName, String key, String defaultValue) {

        return context.getSharedPreferences(spName, Context.MODE_PRIVATE)
                .getString(key, defaultValue);


    }


}
