package com.merpyzf.transfermanager.send;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.merpyzf.transfermanager.P2pTransferHandler;
import com.merpyzf.transfermanager.common.Const;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by wangke on 2017/12/12.
 * <p>
 * 处理发送任务
 */

public class SenderTaskImp implements SendTask, Runnable {

    private List<FileInfo> mSendFileList;
    private Socket mSocket;
    private OutputStream mOutputStream;
    private P2pTransferHandler mP2pTransferHandler;
    private long start_cal_speed;
    private long end_cal_speed;
    private Context mContext;
    private String mDestAddress;
    private String TAG = SenderTaskImp.class.getSimpleName();

    /**
     * 文件发送时，先发送待传输的文件列表,紧接着传送文件的 缩略图( 各种格式 -> bitmap -> byte[])
     * 发送文件 （头信息 + 内容部分）
     */

    public SenderTaskImp(Context context, String destAddress, List<FileInfo> sendFileList, P2pTransferHandler p2pTransferHandler) {
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
     * 发送等待传输的文件清单
     */
    @Override
    public void sendTransferFileList() {

        // 文件类型
        // 文件名
        // 缩略图大小
        // 发送结束标记
        // 缩略图


        for (int i = 0; i < mSendFileList.size(); i++) {
            byte[] FileThumbArray = null;

            StringBuilder sb = new StringBuilder();
            FileInfo file = mSendFileList.get(i);

            //文件类型
            sb.append(file.getType());
            sb.append(Const.S_SEPARATOR);

            //文件名
            sb.append(file.getName());
            sb.append(Const.S_SEPARATOR);

            // 后缀名
            sb.append(file.getSuffix());
            sb.append(Const.S_SEPARATOR);

            // 文件大小
            sb.append(file.getLength());
            sb.append(Const.S_SEPARATOR);


            int fileThumbLength = 0;


            if (file.getType() != FileInfo.FILE_TYPE_IMAGE) {
                // 缩略图大小(耗时操作)
                FileThumbArray = FileUtils.getFileThumbByteArray(mContext, file);
                fileThumbLength = FileThumbArray.length;
            }

            sb.append(fileThumbLength);
            sb.append(Const.S_SEPARATOR);

            // 文件MD5值
            sb.append(file.getMd5());
            sb.append(Const.S_SEPARATOR);

            //最后一个文件
            if (i == mSendFileList.size() - 1) {

                sb.append("1");
                sb.append(Const.S_SEPARATOR);

            } else {
                sb.append("-1");
                sb.append(Const.S_SEPARATOR);
            }

            sb.append(Const.S_END);

            int currentLength = sb.toString().getBytes().length;

            if (currentLength < Const.FILE_THUMB_HEADER_LENGTH) {
                // 少于的部分使用空格填充
                for (int j = 0; j < Const.FILE_THUMB_HEADER_LENGTH - currentLength; j++) {
                    sb.append(" ");
                }
            }
            try {


                if (mOutputStream == null) {
                    return;
                }


                Log.i("wk", "写出->" + sb.toString());

                // 写出头信息
                mOutputStream.write(sb.toString().getBytes());
                if (file.getType() != FileInfo.FILE_TYPE_IMAGE) {
                    // 写出缩略图
                    mOutputStream.write(FileThumbArray);
                }

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
            mOutputStream.write(header.getBytes(Const.S_CHARSET));
            mOutputStream.flush();
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
        byte[] buffer = new byte[Const.BUFFER_LENGTH];

        int readLength = 0;
        // 每一次读取的字节长度
        int currentLength;

        long start = System.currentTimeMillis();

        start_cal_speed = System.currentTimeMillis();


        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));

            // 设置传输状态为: 传输中
            fileInfo.setFileTransferStatus(Const.TransferStatus.TRANSFING);
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
                    // 传输进度
                    fileInfo.setProgress((readLength / (totalLength * 1.0f)));

                    // 传输进度
                    Message message = mP2pTransferHandler.obtainMessage();
                    message.what = Const.TransferStatus.TRANSFING;
                    message.obj = fileInfo;
                    mP2pTransferHandler.sendMessage(message);
                    start = end;
                }

                // TODO: 2018/1/18 这边需要计算文件传输的进度，和传输的速率
            }
            mOutputStream.flush();
            fileInfo.setFileTransferStatus(Const.TransferStatus.TRANSFER_SUCCESS);
            Message message = mP2pTransferHandler.obtainMessage();
            message.what = Const.TransferStatus.TRANSFER_SUCCESS;
            message.obj = fileInfo;
            mP2pTransferHandler.sendMessage(message);


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
            if(mOutputStream==null){
                return;
            }
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {

        // 建立Socket连接
        try {
            Log.i("wk", "等待建立连接的对端设备地址->" + mDestAddress);

            while (true) {

                mSocket = new Socket(mDestAddress, Const.SOCKET_PORT);
                if (mSocket != null) {
                    break;
                }
            }

            // TODO: 2018/4/10 建立连接失败的话mSocket对象会为null

            if(mSocket == null){

                Log.i("wk", "socket是空的");
            }

            Log.i("wk", "建立socket连接，对端地址->" + mSocket.getInetAddress().getHostAddress());
            init();
            /**
             * 发送文件列表和缩略图文件
             */
            sendTransferFileList();
            for (int i = 0; i < mSendFileList.size(); i++) {

                FileInfo fileInfo = mSendFileList.get(i);
                fileInfo.setIsLast(-1);

                // 标记发送的文件是最后一个文件
                if (i == mSendFileList.size() - 1) {
                    fileInfo.setIsLast(1);
                }

                sendFile(fileInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送文件
     */
    void sendFile(FileInfo fileInfo) {
        sendHeader(fileInfo);
        sendBody(fileInfo);
    }

}
