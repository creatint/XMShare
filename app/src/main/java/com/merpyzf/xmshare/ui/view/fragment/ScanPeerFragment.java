package com.merpyzf.xmshare.ui.view.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.httpcoreserver.util.NetworkUtil;
import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.ui.adapter.PeerAdapter;
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ScanPeerFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {
    private Context mContext;
    private Unbinder mUnbinder;
    private PeerManager mPeerManager;
    private List<Peer> mPeerList = new ArrayList<>();
    @BindView(R.id.rv_peers)
    RecyclerView mRvPeerList;
    private PeerAdapter mPeerAdapter;
    private OnPairActionListener mOnPairActionListener;

    // 要建立连接的对端设备
    private Peer mPeerRequestConn;

    public ScanPeerFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scan_peer, container, false);
        mContext = getActivity();
        mUnbinder = ButterKnife.bind(this, rootView);

        initUI();
        mPeerAdapter = new PeerAdapter(R.layout.item_rv_send_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);

        mPeerAdapter.setOnItemClickListener(this);


        String nickName = SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");

        mPeerManager = new PeerManager(mContext, nickName, new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {
                if (!mPeerList.contains(peer)) {

                    mPeerList.add(peer);
                    mPeerAdapter.notifyDataSetChanged();
                    Log.i("w2k", "有新设备上线了: " + peer.getNickName());

                }

            }

            @Override
            public void onDeviceOffLine(Peer peer) {
                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    mPeerAdapter.notifyDataSetChanged();
                    Log.i("w2k", "设备离线了");
                    Toast.makeText(mContext, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onRequestConnect(Peer peer) {

            }


            @Override
            public void onAnswerRequestConnect(Peer peer) {

                Log.i("wk", "onAnswerRequestConnect方法执行\n peer: " + peer.getHostAddress());

                if (peer.equals(mPeerRequestConn)) {

                    Toast.makeText(mContext, "验证成功,开始建立连接", Toast.LENGTH_SHORT).show();

                    // TODO: 2018/1/14 在这边开始建立Socket连接，并发送文件，切换到文件传输的界面
                    if (mOnPairActionListener != null) {
                        mOnPairActionListener.onPeerPairSuccessAction(peer);
                    }


                } else {

                    if (mOnPairActionListener != null) {
                        mOnPairActionListener.onPeerPairFailedAction(peer);
                    }

                    Toast.makeText(mContext, "Peer不匹配，验证失败", Toast.LENGTH_SHORT).show();

                }

                // 将界面切换到文件传输的Fragment，根据peer中的主机地址连接到指定的那个主机
            }

            @Override
            public void onTransferBreak(Peer peer) {

            }
        });

        /**
         * 此处建立的是一个接收UDP信息的服务端
         */
        mPeerManager.listenBroadcast();

        return rootView;
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
    }

    /**
     * RecyclerView 列表中item点击的回调事件
     *
     * @param adapter
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {


        mPeerRequestConn = (Peer) adapter.getItem(position);
        // 构造UDP消息的内容
        SignMessage signMessage = new SignMessage();
        signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
        signMessage.setNickName(SharedPreUtils.getNickName(mContext));
        signMessage.setMsgContent("建立连接请求");
        signMessage.setCmd(SignMessage.cmd.REQUEST_CONN);
        String msg = signMessage.convertProtocolStr();


        InetAddress dest = null;
        try {
            dest = InetAddress.getByName(mPeerRequestConn.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // 将消息发送给对端
        mPeerManager.send2Peer(msg, dest, com.merpyzf.transfermanager.constant.Constant.UDP_PORT);
        if(mOnPairActionListener !=null){
            mOnPairActionListener.onSendConnRequestAction();
        }
        Toast.makeText(mContext, "发送建立请求连接", Toast.LENGTH_SHORT).show();

        // 在屏幕中间显示一个火箭的图标
        // 将这个事件回调给Activity


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
        // 发送下线广播
        mPeerManager.sendOffLineBroadcast();
        // 关闭UdpServer端，停止接收数据
        mPeerManager.stopUdpServer();
        super.onDestroy();
    }

    public interface OnPairActionListener {

        /**
         * 发送连接的请求
         */
        void onSendConnRequestAction();

        /**
         * 配对成功
         *
         * @param peer
         */
        void onPeerPairSuccessAction(Peer peer);

        /**
         * 配对失败
         *
         * @param peer
         */
        void onPeerPairFailedAction(Peer peer);


    }

    public void setOnPeerActionListener(OnPairActionListener onPairActionListener) {
        this.mOnPairActionListener = onPairActionListener;
    }
}
