package com.merpyzf.xmshare;

import com.merpyzf.transfermanager.util.FileUtils;
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

    @Test
    public void testThread(){

        ThreadCtrDemo threadCtrDemo = new ThreadCtrDemo();
        threadCtrDemo.start();


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2秒之后线程转向挂起状态
        threadCtrDemo.mySuspend();


        try {
            // 线程挂起2秒钟
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 两秒钟之后线程中的任务继续执行
        threadCtrDemo.myResume();


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2秒钟之后，停止线程任务
        threadCtrDemo.myStop();

    }





    class ThreadCtrDemo extends Thread{

        // 停止
        private final int STOP = -1;
        // 延迟
        private final int SUSPEND = 0;
        // 运行
        private final int RUNNING = 1;
        // 记录当前线程运行的状态
        private int status = 1;
        private long count = 0;


        @Override
        public synchronized void run() {
            super.run();

            while (status!=STOP){


                count++;

                if(status == SUSPEND){

                    try {

                        System.out.println("线程准备要暂停了");
                        // 阻塞
                        wait();


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }else {

                    count++;
                    System.out.println(count);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



            }

        }

        /**
         * 恢复
         */
        public synchronized void myResume()
        {
            // 修改状态
            status = RUNNING;
            // 唤醒
            notifyAll();
        }

        /**
         * 挂起
         */
        public void mySuspend()
        {
            // 修改状态
            status = SUSPEND;
        }

        /**
         * 停止
         */
        public void myStop()
        {
            // 修改状态
            status = STOP;
        }
    }

    @Test
    public void textSplitSuffix(){

        String fileSuffix = FileUtils.getFileSuffix("/storage/emulated/0/xmshare/receive/apk/手机京东.apk");

        System.out.println(fileSuffix);

    }
}
