package com.merpyzf.xmshare;

import com.merpyzf.transfermanager.util.FormatUtils;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest{

    @Test
    public void testStr2Unicode(){

        String name = "1";

        String unicode = FormatUtils.string2Unicode(name);

        System.out.println("unicode:  "+unicode);

    }

}