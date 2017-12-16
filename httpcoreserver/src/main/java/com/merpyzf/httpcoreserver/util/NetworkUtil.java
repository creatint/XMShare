package com.merpyzf.httpcoreserver.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by wangke on 2017/12/1.
 * 网络相关的Util
 */

public class NetworkUtil {

    private static final String TAG = "NetworkUtil";


    public static String getLocalIp(Context context){

        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        // 检查wifi是否开启
        if(!wifiManager.isWifiEnabled()){

            return null;

        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String ip = intToIp(wifiInfo.getIpAddress());

        LogUtil.i(TAG, "当前设备的IP地址"+ip);

        return ip;


    }

    private static String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
                + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }









}
