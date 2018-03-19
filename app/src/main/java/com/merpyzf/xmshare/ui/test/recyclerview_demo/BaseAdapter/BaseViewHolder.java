package com.merpyzf.xmshare.ui.test.recyclerview_demo.BaseAdapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

/**
 * Created by wangke on 2018/2/9.
 */

public class BaseViewHolder extends RecyclerView.ViewHolder {
    // 子布局中的控件
    private SparseArray<View> mItemViews;
    private View mRootView;

    // 初始化ViewHolder
    public BaseViewHolder(View itemView) {
        super(itemView);
        mItemViews = new SparseArray<>();
        mRootView = itemView;
    }

    /**
     * 获取子View
     * @param viewId
     * @return
     */
    public View getView(int viewId) {

        View view = mItemViews.get(viewId);
        if (view == null) {
            view = mRootView.findViewById(viewId);
        }

        return view;
    }

    /**
     * 给TextView设置文本
     * @param viewId
     * @param text
     * @return
     */
    public BaseViewHolder setText(int viewId, String text){

        TextView textView = (TextView) getView(viewId);
        textView.setText(text);
        return this;

    }
}
