package com.merpyzf.xmshare.bean;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.bean.model.LitepalFileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangke on 2018/4/16.
 */

public class FileInfoFactory {


    public static List<FileInfo> toFileInfoList(List<LitepalFileInfo> litepalFileInfos) {

        List<FileInfo> fileInfoList = new ArrayList<>();
        for (LitepalFileInfo litepalFileInfo : litepalFileInfos) {
            fileInfoList.add(toFileInfo(litepalFileInfo));
        }

        return fileInfoList;
    }


    public static List<LitepalFileInfo> toLitepalFileInfoList(List<FileInfo> fileInfos) {

        List<LitepalFileInfo> litepalFileInfoList = new ArrayList<>();

        for (FileInfo fileInfo : fileInfos) {

            litepalFileInfoList.add(toLitepalFileInfo(fileInfo));
        }

        return litepalFileInfoList;
    }

    public static FileInfo toFileInfo(LitepalFileInfo litepalFileInfo) {

        FileInfo fileInfo = new FileInfo();

        fileInfo.setName(litepalFileInfo.getName());
        fileInfo.setPath(litepalFileInfo.getPath());
        fileInfo.setSuffix(litepalFileInfo.getSuffix());
        fileInfo.setLength(litepalFileInfo.getLength());
        fileInfo.setType(litepalFileInfo.getType());

        return fileInfo;
    }


    public static LitepalFileInfo toLitepalFileInfo(FileInfo fileInfo) {

        LitepalFileInfo litepalFileInfo = new LitepalFileInfo();
        litepalFileInfo.setName(fileInfo.getName());
        litepalFileInfo.setPath(fileInfo.getPath());
        litepalFileInfo.setLength(fileInfo.getLength());
        litepalFileInfo.setSuffix(fileInfo.getSuffix());
        litepalFileInfo.setType(fileInfo.getType());

        return litepalFileInfo;


    }

}
