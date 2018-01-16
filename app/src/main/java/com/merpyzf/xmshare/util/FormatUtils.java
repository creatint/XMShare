package com.merpyzf.xmshare.util;

import java.text.DecimalFormat;

/**
 * Created by wangke on 2018/1/16.
 */

public class FormatUtils {

    /**
     * 将字byte转换为MB
     *
     * @param
     * @return
     */
    public static float convert2Mb(long b) {
        float fileSize = b / (1024 * 1024 * 1f);
        DecimalFormat decimalFormat = new DecimalFormat(".0");
        return Float.valueOf(decimalFormat.format(fileSize));
    }


}
