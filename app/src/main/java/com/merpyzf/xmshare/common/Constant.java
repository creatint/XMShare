package com.merpyzf.xmshare.common;

/**
 * Created by wangke on 2017/12/23.
 */

public class Constant {

    // sp文件, 存储用户信息和用户设置的信息
    public static final String SP_USER = "sp_user_info";
    // 传输模式
    public static final String KEY_TRANSFER_MODE = "transfer_mode";

    // 使用已连接的局域网传输
    public static final int TRANSFER_MODE_LAN = -1;
    // 通过建立热点组件局域网的方式传输
    public static final int TRANSFER_MODE_AP = 1;
}
