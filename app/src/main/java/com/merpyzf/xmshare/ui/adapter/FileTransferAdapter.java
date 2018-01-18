package com.merpyzf.xmshare.ui.adapter;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.xmshare.R;

import java.io.File;
import java.util.List;

/**
 * Created by wangke on 2018/1/18.
 */

public class FileTransferAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {

    public FileTransferAdapter(int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, T item) {

        FileInfo fileInfo = (FileInfo) item;


        if (item instanceof ApkFile) {

            ApkFile apkFile = (ApkFile) item;

        } else if (item instanceof MusicFile) {


            MusicFile musicFile = (MusicFile) item;


        } else if (item instanceof PicFile) {


            PicFile picFile = (PicFile) item;


        } else if (item instanceof VideoFile) {

            VideoFile videoFile = (VideoFile) item;


        }


        helper.setText(R.id.tv_title, fileInfo.getName());

        ImageView civ = helper.getView(R.id.iv_file_thumb);
        File thumbFile = new File(Environment.getExternalStorageDirectory().getPath()
                + com.merpyzf.transfermanager.constant.Constant.THUMB_RECEIVE, fileInfo.getName());

        if (thumbFile.exists()) {
            //设置封面图片
            Glide.with(mContext)
                    .load(thumbFile)
                    .crossFade()
                    .centerCrop()
                    .into(civ);
        }


    }


}
