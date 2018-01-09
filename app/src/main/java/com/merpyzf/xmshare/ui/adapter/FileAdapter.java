package com.merpyzf.xmshare.ui.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.ui.entity.ApkFile;
import com.merpyzf.xmshare.ui.entity.MusicFile;
import com.merpyzf.xmshare.ui.entity.PicFile;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by wangke on 2017/12/24.
 */

public class FileAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {
    private Context mContext;

    public FileAdapter(Context context, int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final T item) {


        if (item instanceof ApkFile) {

            ApkFile apkFile = (ApkFile) item;
            ImageView imageView = helper.getView(R.id.iv_apk_ico);
            helper.setText(R.id.tv_apk_name, apkFile.getName());
            imageView.setImageDrawable(apkFile.getApkDrawable());

        } else if (item instanceof MusicFile) {


            MusicFile musicFile = (MusicFile) item;

            helper.setText(R.id.tv_title, musicFile.getName());
            float size = musicFile.getSize() / (1024 * 1024 * 1f);
            DecimalFormat decimalFormat = new DecimalFormat(".0");
            helper.setText(R.id.tv_artist, "artist: " + musicFile.getArtist());
            helper.setText(R.id.tv_size, "size:" + decimalFormat.format(size) + " MB");
            File albumFile = new File(Environment.getExternalStorageDirectory().getPath()
                    + Constant.ALBUM_IMG_PATH, String.valueOf(musicFile.getAlbumId()));
            if (albumFile.exists()) {
                //设置封面图片
                Glide.with(mContext)
                        .load(albumFile)
                        .crossFade()
                        .centerCrop()
                        .into((ImageView) helper.getView(R.id.iv_music_album));
            }


        } else if (item instanceof PicFile) {


            PicFile picFile = (PicFile) item;
            Glide.with(mContext)
                    .load(picFile.getPath())
                    .crossFade()
                    .centerCrop()
                    .into((ImageView) helper.getView(R.id.iv_gallery));

        }


        CheckBox cb = helper.getView(R.id.rb_check);

        if (cb != null) {

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        Log.i("wk", "文件被选中");
                        // TODO: 2018/1/9 将选择的事件回调给外界


                    } else {


                    }


                }
            });

        }


    }


}
