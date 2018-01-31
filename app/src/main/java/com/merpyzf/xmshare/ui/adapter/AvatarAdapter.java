package com.merpyzf.xmshare.ui.adapter;

import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.xmshare.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by wangke on 2018/1/31.
 */

public class AvatarAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {


    public AvatarAdapter(int layoutResId, @Nullable List<Integer> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Integer item) {

        Glide.with(mContext)
                .load(item)
                .crossFade()
                .centerCrop()
                .into((CircleImageView) helper.getView(R.id.civ_avatar));
    }


}
