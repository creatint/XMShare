package com.merpyzf.transfermanager.receive;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.merpyzf.transfermanager.P2pTransferHandler;
import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.transfermanager.util.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangke on 2017/12/22.
 */

public class ReceiveTask implements Runnable, IReceiveTask {

    private Socket mSocketClient;
    private InputStream mInputStream;
    private P2pTransferHandler mP2pTransferHandler;
    private List<FileInfo> mReceiveFileList;
    private long start_cal_speed;
    private long end_cal_speed;
    private String TAG = ReceiveTask.class.getSimpleName();
    private boolean isStop = false;

    public ReceiveTask(Socket socket, P2pTransferHandler receiveHandler) {
        this.mSocketClient = socket;
        this.mP2pTransferHandler = receiveHandler;
        init();
    }


    @Override
    public void init() {
        try {
            mInputStream = mSocketClient.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收待传输文件列表
     * 文件名和缩略图
     */
    @Override
    public synchronized void receiveTransferFileList() {


        byte[] buffer = new byte[Constant.FILE_THUMB_HEADER_LENGTH];
        mReceiveFileList = new ArrayList<>();
        while (true) {
            int read = 0;
            try {
                // 1024个字节
                read = mInputStream.read(buffer, 0, buffer.length);
                String str = new String(buffer, Constant.S_CHARSET);
                // 拆分前面的数据部分
                String strHeader = str.substring(0, str.indexOf(Constant.S_END));
                String[] split = strHeader.split(":");
                // 文件类型
                int fileType = Integer.valueOf(split[0]);
                // 文件名
                String name = split[1];
                int thumbLength = Integer.valueOf(split[2]);
                // 标记是否是最后一个待接收的文件
                int isLast = Integer.valueOf(split[3]);

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File parentfile = new File(Environment.getExternalStorageDirectory() + Constant.THUMB_RECEIVE);
                    if (!parentfile.exists()) {

                        parentfile.mkdirs();
                    }

                    File file = new File(parentfile, name);
                    FileUtils.writeStream2SdCard(file, mInputStream, thumbLength);
                }


                switch (fileType) {

                    case FileInfo.FILE_TYPE_APP:

                        ApkFile apkFile = new ApkFile();
                        apkFile.setName(name);
                        apkFile.setType(FileInfo.FILE_TYPE_APP);
                        mReceiveFileList.add(apkFile);

                        break;

                    case FileInfo.FILE_TYPE_IMAGE:


                        PicFile picFile = new PicFile();
                        picFile.setName(name);
                        picFile.setType(FileInfo.FILE_TYPE_IMAGE);
                        mReceiveFileList.add(picFile);

                        break;

                    case FileInfo.FILE_TYPE_MUSIC:


                        MusicFile musicFile = new MusicFile();
                        musicFile.setName(name);
                        musicFile.setType(FileInfo.FILE_TYPE_MUSIC);
                        mReceiveFileList.add(musicFile);


                        break;

                    case FileInfo.FILE_TYPE_VIDEO:


                        VideoFile videoFile = new VideoFile();
                        videoFile.setName(name);
                        videoFile.setType(FileInfo.FILE_TYPE_VIDEO);
                        mReceiveFileList.add(videoFile);

                        break;

                    default:
                        break;

                }
                // 如果当前接收的这个文件就已经是最后一个则跳出while循环
                if (isLast == 1) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Message message = mP2pTransferHandler.obtainMessage();
        // 通知待接收文件列表传输完成
        message.what = Constant.TransferStatus.TRANSFER_FILE_LIST_SUCCESS;
        message.obj = mReceiveFileList;
        mP2pTransferHandler.sendMessage(message);
    }

    @Override
    public void run() {
        receiveTransferFileList();
        // 用于计算传输的速度
        start_cal_speed = System.currentTimeMillis();

        while (!isStop) {

            FileInfo fileInfo = parseHeader();
            if(fileInfo == null){
                return;
            }

            readBody(fileInfo);
            if (fileInfo.getIsLast() == 1) {
                break;
            }

        }

        Log.i(TAG, "文件发送完毕");
    }

    @Override
    public synchronized FileInfo parseHeader() {

        byte[] headerBytes = new byte[Constant.HEADER_LENGTH];
        int headCurrent = 0;
        int readByte = -1;

        try {
            // 这边需要按照字节精确读取，直接读入缓冲区会出现错误
            while ((readByte = mInputStream.read()) != -1) {
                headerBytes[headCurrent] = (byte) readByte;
                headCurrent++;
                if (headCurrent == headerBytes.length) {
                    break;
                }
            }

            String str = new String(headerBytes, Constant.S_CHARSET);
            Log.i(TAG, "头信息: " + str);

            // 解析到的头部字符串
            String header = str.substring(0, str.indexOf(Constant.S_END));

            String[] split = header.split(":");

            // 文件类型
            int type = Integer.valueOf(split[0]);
            // 文件名
            String name = split[1];
            // 文件大小
            int length = Integer.valueOf(split[2]);
            String suffix = split[3];
            Integer isLast = Integer.valueOf(split[4]);


            FileInfo fileInfo = null;
            for (FileInfo file : mReceiveFileList) {
                if (file.getName().equals(name)) {
                    fileInfo = file;
                }
            }

            // 设置文件大小
            fileInfo.setLength(length);
            // 设置文件后缀名
            fileInfo.setSuffix(suffix);
            fileInfo.setIsLast(isLast);
            // 设置文件的传输状态为传输中
            fileInfo.setFileTransferStatus(Constant.TransferStatus.TRANSFING);

            return fileInfo;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 进行文件部分字节的读取
     *
     * @param fileInfo
     */
    @Override
    public synchronized void readBody(FileInfo fileInfo) {

        if(fileInfo == null){
            return;
        }
        // 读取文件的总长度
        int totalLength = fileInfo.getLength();
        int currentLength = 0;
        int readLength = -1;
        int perSecondReadLength = 0;

        byte[] buffer = new byte[Constant.BUFFER_LENGTH];
        // 设置文件的传输状态
        fileInfo.setFileTransferStatus(Constant.TransferStatus.TRANSFING);
        // TODO: 2018/1/18 这个方法名需需要修改
        File saveFile = FileUtils.getSaveFile(fileInfo);
        BufferedOutputStream bos = null;
        long start = System.currentTimeMillis();
        start_cal_speed = System.currentTimeMillis();


        try {
            bos = new BufferedOutputStream(new FileOutputStream(saveFile));
            while (currentLength < totalLength) {
                // 剩下的未读取的字节
                int leftLength = totalLength - currentLength;
                if (leftLength >= Constant.BUFFER_LENGTH) {
                    readLength = mInputStream.read(buffer, 0, Constant.BUFFER_LENGTH);
                } else {
                    readLength = mInputStream.read(buffer, 0, leftLength);
                }
                bos.write(buffer, 0, (int) readLength);
                currentLength += readLength;
                perSecondReadLength += readLength;
                long end = System.currentTimeMillis();
                end_cal_speed = System.currentTimeMillis();
                if (end_cal_speed - start_cal_speed >= 1000) {
                    String[] transferSpeed = FileUtils.getFileSizeArrayStr(perSecondReadLength);
                    fileInfo.setTransferSpeed(transferSpeed);
                    // 重置
                    perSecondReadLength = 0;
                    start_cal_speed = end_cal_speed;
                }


                if (end - start >= 50) {
                    float progress = currentLength / (totalLength * 1.0f);
                    fileInfo.setProgress(progress);
                    Message message = mP2pTransferHandler.obtainMessage();
                    message.obj = fileInfo;
                    // 标记传输中
                    message.what = Constant.TransferStatus.TRANSFING;
                    mP2pTransferHandler.sendMessage(message);
                    start = end;
                }


            }
            bos.flush();
            // 标记文件传输成功
            fileInfo.setFileTransferStatus(Constant.TransferStatus.TRANSFER_SUCCESS);
            // 回调通知外界文件传输完成
            Message message = mP2pTransferHandler.obtainMessage();
            message.what = Constant.TransferStatus.TRANSFER_SUCCESS;
            message.obj = fileInfo;
            mP2pTransferHandler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {

            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void release() {
        isStop = true;
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
        }

    }


    // TODO: 2018/1/22 在传输过程中一方退出时传输中断的问题尚未处理，以及资源的释放


}
