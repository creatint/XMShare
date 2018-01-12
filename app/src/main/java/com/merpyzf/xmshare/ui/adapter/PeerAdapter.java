package com.merpyzf.xmshare.ui.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.xmshare.R;

import java.util.List;

/**
 * Created by wangke on 2018/1/12.
 */

public class PeerAdapter extends BaseQuickAdapter<Peer, BaseViewHolder>{

    public PeerAdapter(int layoutResId, @Nullable List<Peer> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Peer item) {
        helper.setText(R.id.tv_nickname, item.getNickName());
        helper.setText(R.id.tv_hostaddress, item.getHostAddress());
    }
}
