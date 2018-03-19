package com.merpyzf.xmshare.ui.test.recyclerview_demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.merpyzf.xmshare.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangke on 2018/2/9.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {


    private List<String> mList = new ArrayList<>();
    private Context mContext = null;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // 这部分代码可以省略
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rv_apk, parent));
        return holder;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.mTvName.setText(mList.get(position));

    }

    @Override
    public int getItemCount() {

        // 这部分代码可以省略
        return mList.size();
    }
}
