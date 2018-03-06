package com.merpyzf.xmshare.ui.view.fragment.transfer;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.send.SenderManager;
import com.merpyzf.transfermanager.util.WifiMgr;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.XMShareApp;
import com.merpyzf.xmshare.ui.adapter.FileTransferAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 文件发送列表进度及状态展示界面
 * @author wangke
 */
@SuppressLint("ValidFragment")
public class TransferSendFragment extends Fragment  {

    @BindView(R.id.rv_send_list)
    RecyclerView mRvSendList;
    private Unbinder mUnbinder;
    private Context mContext;
    private FileTransferAdapter<FileInfo> mFileTransferAdapter;
    private String mNickName;
    private WifiMgr mWifiMgr;
    private Peer mPeer;
    private static final String TAG = TransferSendFragment.class.getSimpleName();
    @SuppressLint("ValidFragment")
    public TransferSendFragment(Peer peer) {
        this.mPeer = peer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_transfer_send, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();
        initUI();

        mFileTransferAdapter = new FileTransferAdapter<>(R.layout.item_rv_transfer,
                FileTransferAdapter.TYPE_SEND, XMShareApp.getSendFileList());
        mRvSendList.setAdapter(mFileTransferAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 当View创建完毕后进行文件的发送
        SenderManager.getInstance(mContext).send(mPeer.getHostAddress(), XMShareApp.getSendFileList());
        super.onViewCreated(view, savedInstanceState);


    }

    private void initUI() {

        mRvSendList.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        SenderManager.getInstance(mContext).release();
        XMShareApp.resetSendFileList();
        super.onDestroy();
    }


}
