package com.merpyzf.transfermanager.entity;

import com.merpyzf.transfermanager.common.Const;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by wangke on 2017/12/23.
 */
public class FileInfo  extends DataSupport implements Serializable {

    /**
     * 应用类型(APK)
     */

    public static final int FILE_TYPE_APP = 0x01;

    /**
     * 图片类型
     */
    public static final int FILE_TYPE_IMAGE = 0x02;

    /**
     * 音乐类型
     */
    public static final int FILE_TYPE_MUSIC = 0x03;

    /**
     * 视频类型
     */
    public static final int FILE_TYPE_VIDEO = 0x04;

    /**
     * 文档类型
     */
    public static final int FILE_TYPE_DOCUMENT = 0x05;


    /**
     * 压缩文件
     */
    public static final int FILE_TYPE_COMPACT = 0x06;



    // 文件id

    private String id;
    // 文件名
    private String name;
    // 文件路径
    private String path;
    // 文件类型
    private int type;
    // 文件的后缀名
    private String suffix;
    // 文件大小
    private int length;

    // 文件传输的进度
    @Column(ignore = true)
    private float progress;

    // 文件传输速度
    @Column(ignore = true)
    private String[] transferSpeed;

    // 默认的传输状态为等待状态
    @Column(ignore = true)
    private int fileTransferStatus = Const.TransferStatus.TRANSFER_WAITING;

    //是否是传输中的最后一个文件，如果是则为1，不是默认为-1
    @Column(ignore = true)
    private int isLast = -1;
    // 文件传输的完整性校验
    @Column(ignore = true)
    private String md5;


    public FileInfo() {
    }

    public FileInfo(String name, String path, int type, int length, String suffix) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.length = length;
        this.suffix = suffix;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean getIsLast() {
        return isLast == 1;
    }

    public void setIsLast(int isLast) {
        this.isLast = isLast;
    }

    public int getFileTransferStatus() {
        return fileTransferStatus;
    }

    public void setFileTransferStatus(int fileTransferStatus) {
        this.fileTransferStatus = fileTransferStatus;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public String[] getTransferSpeed() {
        return transferSpeed;
    }

    public void setTransferSpeed(String[] transferSpeed) {
        this.transferSpeed = transferSpeed;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * 获取传输中的文件头信息
     *
     * @return
     */
    public String getHeader() {


        StringBuffer sb = new StringBuffer();
        //文件类型
        sb.append(type);
        sb.append(":");
        // 文件名
        sb.append(name);
        sb.append(":");
        //文件大小
        sb.append(length);
        sb.append(":");
        // 文件后缀
        sb.append(suffix);
        sb.append(":");
        // 文件是否是最后一个的标记
        sb.append(isLast);

        sb.append(Const.S_END);

        // 当前文件头部的长度
        int currentLength = sb.toString().getBytes().length;

        if (currentLength < Const.HEADER_LENGTH) {
            // 少于的部分使用空格填充
            for (int i = 0; i < Const.HEADER_LENGTH - currentLength; i++) {

                sb.append(" ");

            }
        }
        return sb.toString();
    }


    /**
     * 重置当前对象的状态
     */
    public void reset() {
        progress = 0;
        setFileTransferStatus(Const.TransferStatus.TRANSFER_WAITING);
    }
}


