package com.merpyzf.xmshare.ui.test.recyclerview_demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.merpyzf.xmshare.R;

/**
 * Created by wangke on 2018/2/9.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView mTvName;

    public MyViewHolder(View itemView) {
        super(itemView);
        mTvName = itemView.findViewById(R.id.tv_apk_name);
    }


}
