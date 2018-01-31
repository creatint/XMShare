package com.merpyzf.xmshare.ui.view.fragment.transfer;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.App;
import com.merpyzf.xmshare.ui.adapter.FileTransferAdapter;
import com.merpyzf.xmshare.util.AppUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class TransferReceiveFragment extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private Unbinder mUnbinder;
    private Context mContext;
    private ReceiverManager mReceiver;
    private String mNickName;
    private List<FileInfo> mTransferFileList;
    private FileTransferAdapter mFileTransferAdapter;
    private static final String TAG = TransferReceiveFragment.class.getSimpleName();

    @SuppressLint("ValidFragment")
    public TransferReceiveFragment(List<FileInfo> transferFileList) {
        this.mTransferFileList = transferFileList;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_transfer_receive, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();

        Log.i(TAG, "待传输的文件长度-> " + mTransferFileList.size());


        init();
        initUI();
        initEvent();

        return rootView;
    }

    private void initEvent() {


        mFileTransferAdapter.setOnItemClickListener((adapter, view, position) -> {


            FileInfo fileInfo = (FileInfo) adapter.getItem(position);

            Log.i("w2k", "待安装文件路径-》" + fileInfo.getPath());

            // -> 调用系统的组件播放

            switch (fileInfo.getType()) {

                case FileInfo.FILE_TYPE_APP:

                    if (fileInfo.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_SUCCESS) {

                        AppUtils.installApk(mContext, new File(Environment.getExternalStorageDirectory() + fileInfo.getPath()));

                    } else {
                        Toast.makeText(mContext, "请等待文件传输完毕后再点击安装", Toast.LENGTH_SHORT).show();
                    }

                    break;

                // 点击查看图片
                case FileInfo.FILE_TYPE_IMAGE:

                    break;


                // 点击播放音乐
                case FileInfo.FILE_TYPE_MUSIC:

                    break;

                // 点击播放视频
                case FileInfo.FILE_TYPE_VIDEO:

                    break;
                default:
                    break;


            }

        });


    }


    /**
     * 初始化
     */
    private void init() {

        if (mTransferFileList == null) {
            mTransferFileList = new ArrayList<>();
        }
    }

    /**
     * 初始化UI
     */
    private void initUI() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFileTransferAdapter = new FileTransferAdapter<>(R.layout.item_rv_transfer, FileTransferAdapter.TYPE_RECEIVE, mTransferFileList);
        mRecyclerView.setAdapter(mFileTransferAdapter);
    }


    @Override
    public void onDestroy() {
        App.getSendFileList().clear();
        super.onDestroy();
    }


}
