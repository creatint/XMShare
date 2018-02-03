package com.merpyzf.transfermanager.send;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.merpyzf.transfermanager.P2pTransferHandler;
import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by wangke on 2017/12/12.
 * <p>
 * 处理发送任务
 */

public class SenderTask implements ISendTask, Runnable {

    private List<FileInfo> mSendFileList;
    private Socket mSocket;
    private OutputStream mOutputStream;
    private P2pTransferHandler mP2pTransferHandler;
    private long start_cal_speed;
    private long end_cal_speed;
    private Context mContext;
    private String mDestAddress;
    private String TAG = SenderTask.class.getSimpleName();

    /**
     * 文件发送时，先发送待传输的文件列表,紧接着传送文件的 缩略图( 各种格式 -> bitmap -> byte[])
     * 发送文件 （头信息 + 内容部分）
     */

    public SenderTask(Context context, String destAddress, List<FileInfo> sendFileList, P2pTransferHandler p2pTransferHandler) {
        this.mSendFileList = sendFileList;
        this.mP2pTransferHandler = p2pTransferHandler;
        this.mContext = context;
        this.mDestAddress = destAddress;
    }


    @Override
    public void init() {

        if (mSocket != null) {
            try {
                mOutputStream = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 发送待传输文件列表(不计入传输的大小中去，传输时间可以小到忽略不计)
     */
    @Override
    public void sendTransferFileList() {

        // 文件类型
        // 文件名
        // 缩略图大小
        // 发送结束标记
        // 缩略图
        Log.i("w2k", "发送文件，文件长度:" + mSendFileList.size());
        for (int i = 0; i < mSendFileList.size(); i++) {

            byte[] FileThumbArray;

            StringBuilder sb = new StringBuilder();
            FileInfo file = mSendFileList.get(i);

            //文件类型
            sb.append(file.getType());
            sb.append(":");

            //文件名
            sb.append(file.getName());
            sb.append(":");

            // 后缀名
            sb.append(file.getSuffix());
            sb.append(":");

            // 文件大小
            sb.append(file.getLength());
            sb.append(":");
            // 缩略图大小
            FileThumbArray = FileUtils.getFileThumbByteArray(mContext, file);
            sb.append(FileThumbArray.length);
            sb.append(":");

            //最后一个文件
            if (i == mSendFileList.size() - 1) {

                sb.append("1");
                sb.append(":");

            } else {
                sb.append("-1");
                sb.append(":");
            }

            sb.append(Constant.S_END);

            int currentLength = sb.toString().getBytes().length;

            Log.i(TAG, "header->"+sb.toString());


            if (currentLength < Constant.FILE_THUMB_HEADER_LENGTH) {
                // 少于的部分使用空格填充
                for (int j = 0; j < Constant.FILE_THUMB_HEADER_LENGTH - currentLength; j++) {
                    sb.append(" ");
                }
            }
            try {

                Log.i("w2k", "写出待发送文件列表:  \n文件名: " + file.getName() + "\n"
                        + "缩略图大小: " + FileThumbArray.length);

                if (mOutputStream == null) {
                    return;
                }
                // 写出头信息
                mOutputStream.write(sb.toString().getBytes());
                // 写出缩略图
                mOutputStream.write(FileThumbArray);

                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    /**
     * 发送文件头信息
     *
     * @param file
     */
    @Override
    public void sendHeader(FileInfo file) {
        // 1024个字节
        String header = file.getHeader();
        try {
            mOutputStream.write(header.getBytes(Constant.S_CHARSET));
            mOutputStream.flush();
            Log.i("w2k", "发送的头信息: " + header);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 发送文件内容部分
     *
     * @param fileInfo
     */
    @Override
    public void sendBody(FileInfo fileInfo) {

        // 计算传输的速度
        String filePath = fileInfo.getPath();
        File file = new File(filePath);
        // 文件传输的总长度
        long totalLength = file.length();
        int perSecondReadLength = 0;
        byte[] buffer = new byte[Constant.BUFFER_LENGTH];

        int readLength = 0;
        // 每一次读取的字节长度
        int currentLength = -1;

        long start = System.currentTimeMillis();

        start_cal_speed = System.currentTimeMillis();


        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));

            // 设置传输状态为: 传输中
            fileInfo.setFileTransferStatus(Constant.TransferStatus.TRANSFING);
            while ((currentLength = bis.read(buffer)) != -1) {

                mOutputStream.write(buffer, 0, currentLength);
                readLength += currentLength;

                long end = System.currentTimeMillis();
                end_cal_speed = System.currentTimeMillis();
                perSecondReadLength += currentLength;

                // 传输速度
                if (end_cal_speed - start_cal_speed >= 1000) {
                    String[] perSecondSpeed = FileUtils.getFileSizeArrayStr(perSecondReadLength);
                    fileInfo.setTransferSpeed(perSecondSpeed);
                    // 重置
                    perSecondReadLength = 0;
                    start_cal_speed = end_cal_speed;
                }

                if (end - start > 50) {

                    Log.i("w2k", "readLength: " + readLength + " totalLength: " + totalLength);

//                    Log.i("w2k", "发送进度: "+(readLength / totalLength*1.0f));
                    // 传输进度
                    fileInfo.setProgress((readLength / (totalLength * 1.0f)));

                    // 传输进度
                    Message message = mP2pTransferHandler.obtainMessage();
                    message.what = Constant.TransferStatus.TRANSFING;
                    message.obj = fileInfo;
                    mP2pTransferHandler.sendMessage(message);
                    start = end;
                }

                // TODO: 2018/1/18 这边需要计算文件传输的进度，和传输的速率
            }
            mOutputStream.flush();
            fileInfo.setFileTransferStatus(Constant.TransferStatus.TRANSFER_SUCCESS);
            Message message = mP2pTransferHandler.obtainMessage();
            message.what = Constant.TransferStatus.TRANSFER_SUCCESS;
            message.obj = fileInfo;
            mP2pTransferHandler.sendMessage(message);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void release() {

        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {

        // 建立Socket连接
        try {
            mSocket = new Socket(mDestAddress, Constant.SOCKET_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();

        /**
         * 发送待传输文件列表和缩略图文件
         */
        sendTransferFileList();

        // 遍历所有待发送的文件列表
        for (int i = 0; i < mSendFileList.size(); i++) {

            FileInfo fileInfo = mSendFileList.get(i);
            fileInfo.setIsLast(-1);

            // 标记发送的文件是最后一个文件
            if (i == mSendFileList.size() - 1) {
                fileInfo.setIsLast(1);
            }
            // 发送文件头信息
            sendHeader(fileInfo);

            // 发送文件
            sendBody(fileInfo);
        }


    }
}
