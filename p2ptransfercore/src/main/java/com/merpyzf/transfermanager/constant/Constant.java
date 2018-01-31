package com.merpyzf.transfermanager.constant;

/**
 * Created by wangke on 2017/12/12.
 */

public class Constant {
    // sp文件, 存储用户信息和用户设置的信息

    // 存放歌曲封面的路径
    public static final String THUMB_MUSIC = "/xmshare/music_thumb";
    public static final String THUMB_VIDEO = "/xmshare/video_thumb";
    // 文件接收列表中显示的被压缩的缩略图
    public static final String THUMB_RECEIVE = "/xmshare/receive_thumb";


    // 接收apk文件的保存路径
    public static final String SAVE_APK_PATH = "/xmshare/receive/apk";
    // 接收音乐类型文件的保存路径
    public static final String SAVE_MUSIC_PATH = "/xmshare/receive/music";
    // 接收图片类型文件的保存路径
    public static final String SAVE_IMAGE_PATH = "/xmshare/receive/image";
    // 接收视频类型文件的保存路径
    public static final String SAVE_VIDEO_PATH = "/xmshare/receive/video";


    // sp文件, 存储用户信息
    public static final String SP_USER = "sp_user_info";

    // 字符读取时结束的标记位
    public static final String S_END = "\0";

    // 传输过程中参数间的分割符
    public static final String S_SEPARATOR = ":";

    public static final int BUFFER_LENGTH = 1024 * 8;

    public static final int UDP_PORT = 8900;

    // 接收端的IP地址
    public static final String HOST_ADDRESS = "192.168.31.184";

    public static final int SOCKET_PORT = 8088;

    // 数据传输中使用的字符集编码
    public static final String S_CHARSET = "utf-8";

    public static final String BROADCAST = "255.255.255.255";

    // 头信息的长度
    public static final int HEADER_LENGTH = 1024;

    public static final int FILE_THUMB_HEADER_LENGTH = 150;

    // 发送缩略图的尺寸
    public static final int SEND_FILE_THUMB_SIZE = 100;


    // 文件在传输过程中的几个状态
    public class TransferStatus {

        //等待状态
        public static final int TRANSFER_WAITING = 0;

        // 正在传输中
        public static final int TRANSFING = 1;

        // 传输成功
        public static final int TRANSFER_SUCCESS = 2;

        // 传输失败
        public static final int TRANSFER_FAILED = 3;

        // 待传输文件列表传输成功
        public static final int TRANSFER_FILE_LIST_SUCCESS = 4;


    }

}
