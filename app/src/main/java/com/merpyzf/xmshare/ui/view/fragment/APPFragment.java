package com.merpyzf.xmshare.ui.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.adapter.FileAdapter;
import com.merpyzf.xmshare.ui.entity.ApkFile;
import com.merpyzf.xmshare.util.ApkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wangke on 2017/11/20.
 * 显示手机中已安装的所有应用的安装包信息
 */

public class APPFragment extends Fragment {

    private List<ApkFile> mApkFileLists;
    private Unbinder mUnbinder;

    @BindView(R.id.rv_apk_list)
    RecyclerView mRvApkList;
    @BindView(R.id.pb_apk_waiting)
    ProgressBar mProgressbar;

    private Handler mHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_app, container, false);

        initUI(rootView);

        mHandler = new ApkHandler();

        new Thread(new Runnable() {
            @Override
            public void run() {

                mApkFileLists = ApkUtils.getApp(getActivity(), getActivity().getPackageManager());
                // 发送一个空的消息，提示扫描完毕
                mHandler.sendEmptyMessage(0);

            }
        }).start();

        return rootView;

    }

    private void initUI(View rootView) {
        mUnbinder = ButterKnife.bind(this, rootView);
        mRvApkList.setLayoutManager(new GridLayoutManager(getContext(), 4));
    }


    class ApkHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.i("wk", "扫描到的应用的长度:" + mApkFileLists.size());

            FileAdapter apkAdapter = new FileAdapter<ApkFile>(getContext(), R.layout.item_apk_rv, mApkFileLists);
            mRvApkList.setAdapter(apkAdapter);
            mProgressbar.setVisibility(View.INVISIBLE);
        }
    }


}
