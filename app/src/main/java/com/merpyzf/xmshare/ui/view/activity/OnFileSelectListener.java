package com.merpyzf.xmshare.ui.view.activity;

import com.merpyzf.transfermanager.entity.FileInfo;

import java.util.List;

/**
 * Created by wangke on 2018/1/15.
 * 文件选择的监听回调
 */

public interface OnFileSelectListener<T> {

    /**
     * 文件选择
     *
     * @param fileInfo
     */
    void onSelected(T fileInfo);

    /**
     * 文件取消选择
     *
     * @param fileInfo
     */
    void onCancelSelected(T fileInfo);

    /**
     * 文件全选
     */
    void onCheckedAll(List<FileInfo> fileInfoList);

    /**
     * 取消文件全选
     *
     * @param fileInfoList
     */
    void onCancelCheckedAll(List<FileInfo> fileInfoList);


}
