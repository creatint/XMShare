package com.merpyzf.xmshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 春水碧于天 on 2018/3/7.
 */

public class Md5Utils {

    /**
     * 获取文件的Md5
     * @param file
     * @return
     */
    public static String getMd5(File file) {
        InputStream inputStream = null;
        byte[] buffer = new byte[2048];

        int numRead = -1;
        MessageDigest md5;

        try {
            inputStream = new FileInputStream(file);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = inputStream.read(buffer)) != -1) {

                md5.update(buffer, 0, numRead);

            }
            inputStream.close();
            inputStream = null;
            return md5ToString(md5.digest());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } finally {

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

    }


    private static String md5ToString(byte[] md5Bytes) {
        StringBuilder hexValue = new StringBuilder();
        for (byte b : md5Bytes) {
            int val = ((int) b) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

}
