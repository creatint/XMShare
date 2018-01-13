package com.merpyzf.transfermanager.constant;

/**
 * Created by wangke on 2017/12/12.
 */

public class Constant {

    // sp文件, 存储用户信息
    public static final String SP_USER = "sp_user_info";

    // 字符读取时结束的标记位
    public static final String S_END = "\0";

    // 传输过程中参数间的分割符
    public static final String S_SEPARATOR = ":";

    public static final int BUFFER_LENGTH = 8192;

    public static final int UDP_PORT = 8900;

    public static final int SOCKET_PORT = 8088;

    // 数据传输中使用的字符集编码
    public static final String S_CHARSET = "utf-8";

    public static final String BROADCAST = "255.255.255.255";



}
