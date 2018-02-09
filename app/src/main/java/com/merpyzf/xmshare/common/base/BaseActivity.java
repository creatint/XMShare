package com.merpyzf.xmshare.common.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wangke on 2017/11/20.
 * <p>
 * Activity基类
 */

public abstract class BaseActivity extends AppCompatActivity {

    private Unbinder mUnbinder;
    protected Context mContext;


    public static void start(Context context, Class activity) {
        context.startActivity(new Intent(context, activity));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局
        setContentView(getLayoutId());
        mContext = this;
        mUnbinder = ButterKnife.bind(this);
        initData();
        // 初始化View
        initViews(savedInstanceState);
        // 初始化ToolBar
        initToolBar();
        // 初始化View的事件
        initEvents();

    }

    /**
     * 获取Layout id
     *
     * @return
     */
    public abstract int getLayoutId();


    /**
     * 初始化数据
     */
    protected void initData() {


    }


    /**
     * 初始化Views
     *
     * @param savedInstanceState
     */
    public abstract void initViews(Bundle savedInstanceState);


    /**
     * 初始化ToolBar
     */
    protected abstract void initToolBar();

    /**
     * 控件事件的初始化
     */
    public abstract void initEvents();


    /**
     * 初始化RecyclerView
     */
    public void initRecyclerView() {
    }

    ;

    /**
     * 显示ProgressBar
     */
    public void showProgressBar() {
    }

    ;

    /**
     * 隐藏ProgressBar
     */
    public void hideProgressBar() {
    }

    ;

    /**
     * 加载数据
     */
    public void loadData() {
    }

    ;

    /**
     * 加载完毕后设置数据显示
     */
    public void finishLoadTask() {
    }

    ;


    @Override
    protected void onDestroy() {
        // 解除绑定
        mUnbinder.unbind();
        super.onDestroy();
    }
}








