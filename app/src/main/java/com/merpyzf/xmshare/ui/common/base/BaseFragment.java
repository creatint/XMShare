package com.merpyzf.xmshare.ui.common.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wangke on 2017/11/20.
 */

public abstract class BaseFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return inflater.inflate(getLayoutId(), container, false);
    }

    /**
     * 获取Fragment布局id
     * @return
     */
    protected abstract int getLayoutId();




}
