package com.merpyzf.xmshare.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.receiver.FileSelectedListChangedReceiver;
import com.merpyzf.xmshare.ui.view.activity.SelectFilesActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by wangke on 2017/12/24.
 */

public class FileSelectAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private Context mContext;
    private List<T> mFileInfoList;

    public FileSelectAdapter(Context context, int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
        this.mContext = context;
        this.mFileInfoList = data;
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
        ImageView imageView = helper.getView(R.id.iv_file_thumb);

        if (item instanceof ApkFile) {

            ApkFile apkFile = (ApkFile) item;
            imageView.setImageDrawable(apkFile.getApkDrawable());


        } else if (item instanceof MusicFile) {


            MusicFile musicFile = (MusicFile) item;
            File albumFile = new File(Environment.getExternalStorageDirectory().getPath()
                    + com.merpyzf.transfermanager.constant.Constant.THUMB_MUSIC, String.valueOf(musicFile.getAlbumId()));
            if (albumFile.exists()) {
                //设置封面图片
                Glide.with(mContext)
                        .load(albumFile)
                        .crossFade()
                        .centerCrop()
                        .into(imageView);
            }


        } else if (item instanceof PicFile) {


            PicFile picFile = (PicFile) item;
            Glide.with(mContext)
                    .load(picFile.getPath())
                    .crossFade()
                    .centerCrop()
                    .into(imageView);


        } else if (item instanceof VideoFile) {


            VideoFile videoFile = (VideoFile) item;
            String videoThumbPath = Environment.getExternalStorageDirectory() + com.merpyzf.transfermanager.constant.Constant.THUMB_VIDEO + "/" + videoFile.getName();
            Glide.with(mContext)
                    .load(new File(videoThumbPath))
                    .crossFade()
                    .centerCrop()
                    .into(imageView);

        }

        ImageView ivRemove = helper.getView(R.id.iv_remove);

        ivRemove.setOnClickListener(v -> {

            App.removeSendFile(fileInfo);
            // 发送文件选择状态改变的应用内广播
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(FileSelectedListChangedReceiver.ACTION));
            notifyDataSetChanged();
            //更新底部标题
            ((SelectFilesActivity) mContext).updateBottomTitle();
        });

    }


    @NonNull
    @Override
    public String getSectionName(int position) {

        return ((FileInfo)(mFileInfoList.get(position))).getName().substring(0,1);
    }
}
