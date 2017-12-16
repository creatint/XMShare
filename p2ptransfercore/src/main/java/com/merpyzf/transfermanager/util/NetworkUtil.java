package com.merpyzf.transfermanager.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by wangke on 2017/12/1.
 * 网络相关的Util
 */

public class NetworkUtil {

    private static final String TAG = "NetworkUtil";

    /**
     * 获取本机ip地址
     * @param context
     * @return
     */
    public static String getLocalIp(Context context){

        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        // 检查wifi是否开启
        if(!wifiManager.isWifiEnabled()){

            return null;

        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String ip = intToIp(wifiInfo.getIpAddress());

        Log.i(TAG, "当前设备的IP地址"+ip);

        return ip;


    }

    private static String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
                + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }

    /**
     * 获取广播地址
     *
     * @param context
     * @return
     * @throws UnknownHostException
     */
    public static InetAddress getBroadcastAddress(Context context)
            throws UnknownHostException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null)
        {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    /**
     * 判断传入的ip地址是否为本机ip
     * @param ip
     * @return
     */
    public static boolean isLocal(String ip){

        String[] localIP = getLocalAllIP();

        for (String s : localIP) {

            if (ip.equals(s)){
                return true;
            }

        }
        return false;
    }


    /**
     * 获取本机硬件设备绑定的所有IP地址
     * @return
     */
    private static String[] getLocalAllIP()
    {
        ArrayList<String> IPs = new ArrayList<String>();

        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && isIpv4Address(ip.getHostAddress()))
                    {
                        IPs.add(ip.getHostAddress());
                    }
                }

            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

        return (String[]) IPs.toArray(new String[]{});
    }


    /**
     * 判断ip地址的格式是否为
     * @param ipAddress
     * @return
     */
    public static boolean isIpv4Address(String ipAddress){
        if(ipAddress==null || ipAddress.length()==0){
            return false;//字符串为空或者空串
        }
        String[] parts=ipAddress.split("\\.");//因为java doc里已经说明, split的参数是reg, 即正则表达式, 如果用"|"分割, 则需使用"\\|"
        if(parts.length!=4){
            return false;//分割开的数组根本就不是4个数字
        }
        for(int i=0;i<parts.length;i++){
            try{
                int n=Integer.parseInt(parts[i]);
                if(n<0 || n>255){
                    return false;//数字不在正确范围内
                }
            }catch (NumberFormatException e) {
                return false;//转换数字不正确
            }
        }
        return true;
    }


}
