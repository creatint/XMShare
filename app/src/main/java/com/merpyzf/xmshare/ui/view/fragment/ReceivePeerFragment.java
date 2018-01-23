package com.merpyzf.xmshare.ui.view.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.radarview.RadarLayout;
import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.transfermanager.util.NetworkUtil;
import com.merpyzf.transfermanager.util.timer.OSTimer;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.ui.adapter.PeerAdapter;
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ReceivePeerFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {


    private PeerManager mPeerManager;
    private Context mContext;
    private ArrayList<Peer> mPeerList;
    private Unbinder mUnbinder;
    @BindView(R.id.radar)
    RadarLayout radar;
    @BindView(R.id.rv_peers)
    RecyclerView mRvPeerList;
    @BindView(R.id.tv_tip)
    TextView mTvTip;
    private OSTimer mOsTimer;
    private PeerAdapter mPeerAdapter;
    private OnReceivePairActionListener mOnReceivePairActionListener;

    public ReceivePeerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_receive_peer, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        this.mContext = getActivity();
        mPeerList = new ArrayList<>();
        initUI();
        mPeerAdapter = new PeerAdapter(R.layout.item_rv_recive_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);
        mPeerAdapter.setOnItemClickListener(this);

        String nickName = com.merpyzf.xmshare.util.SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");
        mPeerManager = new PeerManager(mContext, nickName, new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {

            }

            @Override
            public void onDeviceOffLine(Peer peer) {

                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    Toast.makeText(mContext, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

                    mPeerAdapter.notifyDataSetChanged();

                }
                checkIsHide();
            }

            @Override
            public void onRequestConnect(Peer peer) {

                if (!mPeerList.contains(peer)) {

                    mPeerList.add(peer);

                }
                checkIsHide();
                Log.i("w2k", "有设备请求建立连接:" + peer.getNickName() + " " + peer.getHostAddress());

                Toast.makeText(mContext, peer.getNickName() + "请求建立连接", Toast.LENGTH_SHORT).show();

                mPeerAdapter.notifyDataSetChanged();
                // TODO: 2018/1/14  开启Socket服务等待设备连接，并开跳转到文件接收的界面，开始接收文件

                if (mOnReceivePairActionListener != null) {
                    mOnReceivePairActionListener.onRequestSendFileAction();
                }


            }

            @Override
            public void onAnswerRequestConnect(Peer peer) {


            }
        });

        mPeerManager.listenBroadcast();
        /**
         * 循环间隔一段时间发送一个上线广播
         */
        mOsTimer = mPeerManager.sendOnLineBroadcast(true);


        return rootView;
    }


    private void checkIsHide() {

        if (mPeerList.size() > 0) {
            mTvTip.setVisibility(View.VISIBLE);
        } else {
            mTvTip.setVisibility(View.INVISIBLE);
        }


    }

    /**
     * 初始化UI
     */
    private void initUI() {

        radar.setDuration(2000);
        radar.setStyleIsFILL(true);
        radar.setRadarColor(Color.GRAY);
        radar.start();

        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
        mTvTip.setVisibility(View.INVISIBLE);

    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        // 发送同意对端发送文件的回应
        Peer peer = (Peer) adapter.getItem(position);
        SignMessage signMessage = new SignMessage();
        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
        signMessage.setCmd(SignMessage.cmd.ANSWER_REQUEST_CONN);
        signMessage.setMsgContent("回应建立连接请求");
        signMessage.setNickName(SharedPreUtils.getNickName(mContext));
        String protocolStr = signMessage.convertProtocolStr();
        try {
            InetAddress dest = InetAddress.getByName(peer.getHostAddress());
            mPeerManager.send2Peer(protocolStr, dest, com.merpyzf.transfermanager.constant.Constant.UDP_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (mOnReceivePairActionListener != null) {
            mOnReceivePairActionListener.onAgreeSendFileAction();
        }
    }


    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        /**
         * 发送下线广播
         */
        mPeerManager.sendOffLineBroadcast();
        mOsTimer.cancel();
        mPeerManager.stopUdpServer();
        // 释放ServerSocket资源
        ReceiverManager.getInstance().release();

        super.onDestroy();
    }


    public interface OnReceivePairActionListener {

        /**
         * 请求发送文件(提前建立ServerSocket)
         */
        void onRequestSendFileAction();

        /**
         * 同意对方发送文件的回调
         */
        void onAgreeSendFileAction();


    }

    public void setOnReceivePairActionListener(OnReceivePairActionListener onReceivePairActionListener) {
        this.mOnReceivePairActionListener = onReceivePairActionListener;
    }
}
