package com.merpyzf.transfermanager.constant;

/**
 * Created by wangke on 2017/12/12.
 */

public class Constant {

    // 字符读取时结束的标记位
    public static final String S_END = "\0";

    // 传输过程中参数间的分割符
    public static final String S_SEPARATOR = ":";

    public static final int BUFFER_LENGTH = 8192;

    public static final int PORT = 8900;

    // 数据传输中使用的字符集编码
    public static final String S_CHARSET = "utf-8";

    public static final String BROADCAST = "255.255.255.255";


    public static class cmd{

        //局域网内设备请求上线(在屏幕上显示局域网内可见的设备)
        public static final int ON_LINE = 1;

        // 设备是上线并建立连接的答复
        public static final int ON_LINE_ANSWER = 2;

        //下线
        public static final int OFF_LINE = 3;
        //请求建立连接
        public static final int REQUEST_CONN = 4;




    }

}
