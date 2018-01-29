package com.merpyzf.xmshare.ui.view.activity;

/**
 * Created by wangke on 2018/1/15.
 * 文件选择的监听回调
 */

public interface OnFileSelectListener<T> {

    /**
     * 文件被选中时的回调
     * @param fileInfo
     */
    void onSelected(T fileInfo);

    /**
     * 文件取消选中时的回调
     * @param fileInfo
     */
    void onCancelSelected(T fileInfo);

    void onCheckedAll();


}
