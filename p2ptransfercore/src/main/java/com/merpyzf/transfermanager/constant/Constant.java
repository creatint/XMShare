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

        //上线
        public static final int ON_LINE = 1;
        //下线
        public static final int OFF_LINE = 2;
        //请求建立连接
        public static final int REQUEST_CONN = 3;




    }

}
