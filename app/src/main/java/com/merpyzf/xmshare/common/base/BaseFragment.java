package com.merpyzf.xmshare.common.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wangke on 2017/11/20.
 */

public abstract class BaseFragment extends Fragment {

    protected View mRootView;

    /**
     * View是否初始化完成
     */
    private boolean isViewCreated;

    /**
     * 数据是否已加载完成
     */
    private boolean isLoadDataCompleted;
    private Unbinder mUnbinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(null == mRootView){

            mRootView = inflater.inflate(getLayoutId(),container, false);
            mUnbinder = ButterKnife.bind(this, mRootView);
        }

        isViewCreated = true;

        return mRootView;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // 当当前Fragment对与用户来说可见，并且视图已经被创建而数据没有加载的时候执行到下面的if代码块中
        if(isVisibleToUser && isViewCreated && !isLoadDataCompleted){

            isLoadDataCompleted = true;

            loadData();


        }



    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getUserVisibleHint()){
            isLoadDataCompleted = true;
            loadData();
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(null!=mRootView){
            // 当视图销毁的时候Fragment的根布局上的View从父视图中移除，避免重复添加报错
            ((ViewGroup)(mRootView.getParent())).removeView(mRootView);
        }

        mUnbinder.unbind();

    }

    /**
     * 加载数据
     */
    protected  void loadData(){
        initView(mRootView);
    }

    /**
     * 初始化View
     * @param mRootView
     */
    protected abstract void initView(View mRootView);

    /**
     * 返回当前Fragment的布局id
     * @return
     */
    protected abstract int getLayoutId();


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
