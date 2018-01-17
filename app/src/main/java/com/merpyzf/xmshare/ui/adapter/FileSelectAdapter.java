package com.merpyzf.xmshare.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.common.base.App;
import com.merpyzf.xmshare.receiver.FileSelectedListChangedReceiver;
import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.xmshare.ui.view.activity.SelectFilesActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by wangke on 2017/12/24.
 */

public class FileSelectAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {
    private Context mContext;

    public FileSelectAdapter(Context context, int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final T item) {

        FileInfo fileInfo = (FileInfo) item;
        float size = fileInfo.getLength() / (1024 * 1024 * 1f);
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        String formatSize = decimalFormat.format(size);


        helper.setText(R.id.tv_title, fileInfo.getName());
        helper.setText(R.id.tv_path, fileInfo.getPath());
        helper.setText(R.id.tv_size, "文件大小:" + formatSize + " MB");


        if (item instanceof ApkFile) {

            ApkFile apkFile = (ApkFile) item;
            ImageView imageView = helper.getView(R.id.iv_file_thumb);
            imageView.setImageDrawable(apkFile.getApkDrawable());

        } else if (item instanceof MusicFile) {


            MusicFile musicFile = (MusicFile) item;
            File albumFile = new File(Environment.getExternalStorageDirectory().getPath()
                    + Constant.THUMB_MUSIC, String.valueOf(musicFile.getAlbumId()));
            if (albumFile.exists()) {
                //设置封面图片
                Glide.with(mContext)
                        .load(albumFile)
                        .crossFade()
                        .centerCrop()
                        .into((ImageView) helper.getView(R.id.iv_file_thumb));
            }


        } else if (item instanceof PicFile) {


            PicFile picFile = (PicFile) item;
            Glide.with(mContext)
                    .load(picFile.getPath())
                    .crossFade()
                    .centerCrop()
                    .into((ImageView) helper.getView(R.id.iv_file_thumb));


        } else if (item instanceof VideoFile) {


            VideoFile videoFile = (VideoFile) item;
            ImageView ivVideoThumb = helper.getView(R.id.iv_file_thumb);
            String videoThumbPath = Environment.getExternalStorageDirectory() + Constant.THUMB_VIDEO + "/" + videoFile.getName();
            Glide.with(mContext)
                    .load(new File(videoThumbPath))
                    .crossFade()
                    .centerCrop()
                    .into(ivVideoThumb);

        }


        ImageView ivRemove = helper.getView(R.id.iv_remove);

        ivRemove.setOnClickListener(v -> {

            App.removeSendFile(fileInfo);
            // 发送文件发生改变的广播
            mContext.sendBroadcast(new Intent(FileSelectedListChangedReceiver.ACTION));
            notifyDataSetChanged();
            //更新底部标题
            ((SelectFilesActivity) mContext).updateBottomTitle();
        });

    }


}
