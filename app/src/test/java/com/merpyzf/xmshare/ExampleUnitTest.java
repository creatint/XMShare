package com.merpyzf.xmshare;

import com.merpyzf.transfermanager.util.FormatUtils;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testStr2Unicode() {

        String name = "1";

        String unicode = FormatUtils.string2Unicode(name);

        System.out.println("unicode:  " + unicode);

    }

    @Test
    public void testPath() {

        String fileName = "这个世界会好吗(2014i/O版)";

        if (fileName.contains("/")) {
            System.out.println("包含/");
            String s = fileName.replaceAll("/", "-");
            System.out.println(s);
        } else {
            System.out.println("不包含");
        }

//        System.out.println(fileName);

    }

}