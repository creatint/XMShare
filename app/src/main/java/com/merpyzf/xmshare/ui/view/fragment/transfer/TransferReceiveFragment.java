package com.merpyzf.xmshare.ui.view.fragment.transfer;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.App;
import com.merpyzf.xmshare.ui.adapter.FileTransferAdapter;
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransferReceiveFragment extends Fragment implements PeerCommunCallback {

    private Unbinder mUnbinder;
    private Context mContext;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ReceiverManager mReceiver;
    private String mNickName;
    private PeerManager mPeerManager;

    public TransferReceiveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_transfer_receive, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();
        mNickName = SharedPreUtils.getNickName(mContext);
        mPeerManager = new PeerManager(mContext, mNickName, this);
        // 开启一个UDPServer
        mPeerManager.listenBroadcast();
        initUI();

        ReceiverManager.getInstance().setOnTransferFileListListener(receiveFileList -> {

            FileTransferAdapter fileTransferAdapter = new FileTransferAdapter<>(R.layout.item_rv_transfer, FileTransferAdapter.TYPE_RECEIVE, receiveFileList);
            mRecyclerView.setAdapter(fileTransferAdapter);

        });


        return rootView;
    }

    /**
     * 初始化UI
     */
    private void initUI() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }


    public void onBackPressed() {
        if (mPeerManager == null) {
            return;
        }
        // 发送传输中断的广播
        mPeerManager.sendTransferBreakBroadcast();
    }

    @Override
    public void onDestroy() {

        // 关闭UDPServer释放资源
        mPeerManager.stopUdpServer();

        App.getSendFileList().clear();
        super.onDestroy();
    }

    @Override
    public void onDeviceOnLine(Peer peer) {

    }

    @Override
    public void onDeviceOffLine(Peer peer) {

    }

    @Override
    public void onRequestConnect(Peer peer) {

    }

    @Override
    public void onAnswerRequestConnect(Peer peer) {

    }

    /**
     * 传输中断
     *
     * @param peer
     */
    @Override
    public void onTransferBreak(Peer peer) {

        Toast.makeText(mContext, "对端 " + peer.getNickName() + "退出了，即将关闭", Toast.LENGTH_SHORT).show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                });


            }
        }, 0);


    }
}
