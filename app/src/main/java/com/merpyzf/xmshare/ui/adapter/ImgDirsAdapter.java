package com.merpyzf.xmshare.ui.adapter;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.util.FileUtils;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.bean.model.PhotoDirBean;

import java.util.List;

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

        ImageView iv_cover = helper.getView(R.id.iv_photo_dir_cover);

        String suffix = FileUtils.getFileSuffix(item.getCoverImg());





        if (suffix.equals("gif") || suffix.equals("GIF")) {

            Log.i("gif","GIF-->"+item.getCoverImg());
            Glide.with(mContext)
                    .load(item.getCoverImg())
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(iv_cover);

        } else {

            Glide.with(mContext)
                    .load(item.getCoverImg())
                    .centerCrop()
                    .into(iv_cover);

        }
    }
}
