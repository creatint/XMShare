package com.merpyzf.xmshare.common;

import com.merpyzf.xmshare.R;

import java.util.ArrayList;

/**
 * Created by wangke on 2017/12/23.
 */

public class Constant {


    // sp文件, 存储用户信息和用户设置的信息
    public static final String SP_USER = "sp_user_info";
    // 数据库名
    public static final String DB_NAME = "xmshare.db";

    // 传输模式
    public static final String KEY_TRANSFER_MODE = "transfer_mode";
    // 使用已连接的局域网传输
    public static final int TRANSFER_MODE_LAN = -1;
    // 通过建立热点组件局域网的方式传输
    public static final int TRANSFER_MODE_AP = 1;

    // 存放头像信息
    public static ArrayList<Integer> AVATAR_LIST = null;

    // 初始化头像
    static {

        AVATAR_LIST = new ArrayList<>();
        AVATAR_LIST.add(R.drawable.ic_avatar_0);
        AVATAR_LIST.add(R.drawable.ic_avatar_1);
        AVATAR_LIST.add(R.drawable.ic_avatar_2);
        AVATAR_LIST.add(R.drawable.ic_avatar_3);
        AVATAR_LIST.add(R.drawable.ic_avatar_4);
        AVATAR_LIST.add(R.drawable.ic_avatar_5);
        AVATAR_LIST.add(R.drawable.ic_avatar_6);
        AVATAR_LIST.add(R.drawable.ic_avatar_7);
        AVATAR_LIST.add(R.drawable.ic_avatar_8);
        AVATAR_LIST.add(R.drawable.ic_avatar_9);
        AVATAR_LIST.add(R.drawable.ic_avatar_10);
        AVATAR_LIST.add(R.drawable.ic_avatar_11);
        AVATAR_LIST.add(R.drawable.ic_avatar_12);
        AVATAR_LIST.add(R.drawable.ic_avatar_13);
        AVATAR_LIST.add(R.drawable.ic_avatar_14);
        AVATAR_LIST.add(R.drawable.ic_avatar_15);


    }


}
