package com.merpyzf.xmshare.ui.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.util.FileUtils;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.bean.model.PhotoDirBean;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by merpyzf on 2018/4/2.
 */

public class ImgDirsAdapter extends BaseQuickAdapter<PhotoDirBean, BaseViewHolder> {


    public ImgDirsAdapter(int layoutResId, @Nullable List<PhotoDirBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PhotoDirBean item) {

        helper.setText(R.id.tv_photo_dir_name, item.getName());
        helper.setText(R.id.tv_photo_num, item.getImageNumber() + "张照片");

        ImageView ivCover = helper.getView(R.id.iv_photo_dir_cover);
        GifImageView gifIv = helper.getView(R.id.gif_photo_dir_cover);

        String suffix = FileUtils.getFileSuffix(item.getCoverImg());


        if (suffix.toLowerCase().equals("gif")) {

            ivCover.setVisibility(View.INVISIBLE);
            gifIv.setVisibility(View.VISIBLE);


            try {
                GifDrawable gifDrawable = new GifDrawable(item.getCoverImg());
                gifIv.setImageDrawable(gifDrawable);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {

            ivCover.setVisibility(View.VISIBLE);
            gifIv.setVisibility(View.INVISIBLE);
            Glide.with(mContext)
                    .load(item.getCoverImg())
                    .centerCrop()
                    .into(ivCover);

        }
    }
}
