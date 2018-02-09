package com.merpyzf.xmshare.ui.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.transfermanager.util.FormatUtils;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.XMShareApp;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

/**
 * Created by wangke on 2017/12/24.
 */

public class FileAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private Context mContext;
    private List<T> mFileList;
    private static final String TAG = FileAdapter.class.getSimpleName();

    public FileAdapter(Context context, int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
        this.mContext = context;
        this.mFileList = data;
    }

    @Override
    protected void convert(BaseViewHolder helper, final T item) {


        if (item instanceof ApkFile) {

            ApkFile apkFile = (ApkFile) item;
            ImageView imageView = helper.getView(R.id.iv_cover);
            helper.setText(R.id.tv_apk_name, apkFile.getName());
            imageView.setImageDrawable(apkFile.getApkDrawable());
            int length = apkFile.getLength();
            helper.setText(R.id.tv_apk_size, FormatUtils.convert2Mb(length) + "MB");


        } else if (item instanceof MusicFile) {


            MusicFile musicFile = (MusicFile) item;

            helper.setText(R.id.tv_title, musicFile.getName());
            helper.setText(R.id.tv_artist, musicFile.getArtist());
            helper.setText(R.id.tv_size, FormatUtils.convert2Mb(musicFile.getLength()) + " MB");

            File albumFile = new File(Environment.getExternalStorageDirectory().getPath()
                    + com.merpyzf.transfermanager.constant.Constant.THUMB_MUSIC, String.valueOf(musicFile.getAlbumId()));
            if (albumFile.exists()) {
                //设置封面图片
                Glide.with(mContext)
                        .load(albumFile)
                        .crossFade()
                        .centerCrop()
                        .into((ImageView) helper.getView(R.id.iv_cover));
            }


        } else if (item instanceof PicFile) {


            PicFile picFile = (PicFile) item;
            Glide.with(mContext)
                    .load(picFile.getPath())
                    .crossFade()
                    .centerCrop()
                    .into((ImageView) helper.getView(R.id.iv_cover));

        } else if (item instanceof VideoFile) {

            VideoFile videoFile = (VideoFile) item;
            helper.setText(R.id.tv_title, videoFile.getName());
            helper.setText(R.id.tv_size, FormatUtils.convert2Mb(videoFile.getLength()) + " MB");
            helper.setText(R.id.tv_duration, FormatUtils.convertMS2Str(videoFile.getDuration()));
            ImageView ivVideoThumb = helper.getView(R.id.iv_cover);
            String videoThumbPath = Environment.getExternalStorageDirectory() + com.merpyzf.transfermanager.constant.Constant.THUMB_VIDEO + "/" + videoFile.getName();

            Glide.with(mContext)
                    .load(new File(videoThumbPath))
                    .crossFade()
                    .centerCrop()
                    .into(ivVideoThumb);

        }

        FileInfo fileInfo = (FileInfo) item;
        ImageView ivSelect = helper.getView(R.id.iv_select);

        if (XMShareApp.getSendFileList().contains(fileInfo)) {
            ivSelect.setVisibility(View.VISIBLE);

        } else {
            ivSelect.setVisibility(View.INVISIBLE);
        }

    }


    @NonNull
    @Override
    public String getSectionName(int position) {

        T fileInfo = mFileList.get(position);

        if (fileInfo instanceof MusicFile) {
            String firstName = ((MusicFile) fileInfo).getName().substring(0, 1);
            Log.i(TAG, "firstName-> " + firstName);
            return firstName;
        } else if (fileInfo instanceof VideoFile) {
            String firstName = ((VideoFile) fileInfo).getName().substring(0, 1);
            Log.i(TAG, "firstName-> " + firstName);
            return firstName;
        }

        return "^_^";
    }
}
