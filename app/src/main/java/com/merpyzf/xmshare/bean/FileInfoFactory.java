package com.merpyzf.xmshare.bean;

import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.CompactFile;
import com.merpyzf.transfermanager.entity.DocFile;
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
            fileInfoList.add(toFileInfoType(litepalFileInfo));
        }

        return fileInfoList;
    }


    public static List<LitepalFileInfo> toLitepalFileInfoList(List<FileInfo> fileInfos) {

        List<LitepalFileInfo> litepalFileInfoList = new ArrayList<>();

        for (FileInfo fileInfo : fileInfos) {

            litepalFileInfoList.add(toLitepalFileInfoType(fileInfo));
        }

        return litepalFileInfoList;
    }

    public static FileInfo toFileInfoType(LitepalFileInfo litepalFileInfo) {

        FileInfo fileInfo = new FileInfo();

        fileInfo.setName(litepalFileInfo.getName());
        fileInfo.setPath(litepalFileInfo.getPath());
        fileInfo.setSuffix(litepalFileInfo.getSuffix());
        fileInfo.setLength(litepalFileInfo.getLength());
        fileInfo.setType(litepalFileInfo.getType());

        return fileInfo;
    }


    public static LitepalFileInfo toLitepalFileInfoType(FileInfo fileInfo) {

        LitepalFileInfo litepalFileInfo = new LitepalFileInfo();
        litepalFileInfo.setName(fileInfo.getName());
        litepalFileInfo.setPath(fileInfo.getPath());
        litepalFileInfo.setLength(fileInfo.getLength());
        litepalFileInfo.setSuffix(fileInfo.getSuffix());
        litepalFileInfo.setType(fileInfo.getType());

        return litepalFileInfo;

    }

    public static ApkFile toApkFileType(LitepalFileInfo litepalFileInfo) {
        ApkFile apkFile = new ApkFile();

        apkFile.setName(litepalFileInfo.getName());
        apkFile.setPath(litepalFileInfo.getPath());
        apkFile.setSuffix(litepalFileInfo.getSuffix());
        apkFile.setLength(litepalFileInfo.getLength());
        apkFile.setType(litepalFileInfo.getType());

        return apkFile;
    }


    public static CompactFile toCompactFileType(LitepalFileInfo litepalFileInfo) {
        CompactFile compactFile = new CompactFile();

        compactFile.setName(litepalFileInfo.getName());
        compactFile.setPath(litepalFileInfo.getPath());
        compactFile.setSuffix(litepalFileInfo.getSuffix());
        compactFile.setLength(litepalFileInfo.getLength());
        compactFile.setType(litepalFileInfo.getType());

        return compactFile;
    }


    public static DocFile toDocFileType(LitepalFileInfo litepalFileInfo) {
        DocFile docFile = new DocFile();

        docFile.setName(litepalFileInfo.getName());
        docFile.setPath(litepalFileInfo.getPath());
        docFile.setSuffix(litepalFileInfo.getSuffix());
        docFile.setLength(litepalFileInfo.getLength());
        docFile.setType(litepalFileInfo.getType());

        return docFile;
    }
}
