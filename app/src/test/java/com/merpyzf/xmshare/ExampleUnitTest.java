package com.merpyzf.xmshare;

import com.merpyzf.httpcoreserver.constant.Constant;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void TestURLDecoder(){


        String url = "http://172.28.67.84:8888/storage/emulated/legacy/%E7%8C%8E%E8%B1%B9%E6%B8%85%E7%90%86%E5%A4%A7%E5%B8%88";

        try {
            // 将Base64编码解码成utf-8编码
            String encodeUrl = URLDecoder.decode(url, Constant.ENCODING);
            System.out.println(encodeUrl);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDot(){

        String fileName = "andro.id";

        //找不到就是返回-1
        int i = fileName.lastIndexOf(".");

        String substring = fileName.substring(i+1);
        System.out.println(substring);
        System.out.println(i);


    }
}