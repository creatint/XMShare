package com.merpyzf.transfermanager.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wangke on 2018/1/25.
 */

public class ApManager {

    /**
     * 获取无线热点当前的状态
     *
     * @return
     */
    public static boolean isApOn(Context context) {

        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {
        }
        return false;

    }

    /**
     * 关闭无线热点
     *
     * @param context
     */
    public static void turnOffAp(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, null, false);
        } catch (Throwable ignored) {

        }
    }

    // toggle wifi hotspot on or off
    public static boolean configApState(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if (wifimanager.isWifiEnabled()) {
                wifimanager.setWifiEnabled(false);
            }

            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // toggle wifi hotspot on or off, and specify the hotspot name
    public static boolean configApState(Context context, String nickName, int avatar) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            wificonfiguration = new WifiConfiguration();
            wificonfiguration.SSID = NetworkUtil.getSSID(nickName, avatar);

            // if WiFi is on, turn it off
            if (wifimanager.isWifiEnabled()) {
                wifimanager.setWifiEnabled(false);
                // if ap is on and then disable ap
                if (isApOn(context)) {
                    turnOffAp(context);
                }
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取热点的ssid
     */
    public static String getApSSID(Context mContext) {

        String ssid = null;

        try {
            WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            //拿到getWifiApConfiguration()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
            //调用getWifiApConfiguration()方法，获取到 热点的WifiConfiguration
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);

            ssid = configuration.SSID;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return ssid;
    }


}
