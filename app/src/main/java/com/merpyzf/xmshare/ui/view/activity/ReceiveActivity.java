package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.radarview.RadarLayout;
import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
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

/**
 * 接收端:
 * <p>
 * 1. 定时发送广播信息，用于发送端对接收端设备的发现
 * 2. 需要开启一个UDPServer,用来显示需要进行连接的发送端的设备，点击发送端设备头像以完成连接确认
 * 3. 当接收端退出的时候,需要发送一个离线广播
 */
public class ReceiveActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {


    private PeerManager mPeerManager;
    private Context mContext;
    private ArrayList<Peer> mPeerList;
    private RadarLayout radar;
    private Unbinder mUnbinder;
    @BindView(R.id.rv_peers)
    RecyclerView mRvPeerList;
    @BindView(R.id.tv_tip)
    TextView mTvTip;
    private OSTimer mOsTimer;
    private PeerAdapter mPeerAdapter;

    public static void start(Context context) {

        context.startActivity(new Intent(context, ReceiveActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        this.mContext = this;
        mPeerList = new ArrayList<>();
        mUnbinder = ButterKnife.bind(this);

        initUI();

        mPeerAdapter = new PeerAdapter(R.layout.item_rv_recive_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);
        mPeerAdapter.setOnItemClickListener(this);


        String nickName = com.merpyzf.xmshare.util.SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");

        mPeerManager = new PeerManager(this, nickName, new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {

            }

            @Override
            public void onDeviceOffLine(Peer peer) {

                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    Toast.makeText(ReceiveActivity.this, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

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

                Toast.makeText(ReceiveActivity.this, peer.getNickName() + "请求建立连接", Toast.LENGTH_SHORT).show();

                mPeerAdapter.notifyDataSetChanged();
                // TODO: 2018/1/14  开启Socket服务等待设备连接，并开跳转到文件接收的界面，开始接收文件


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

        radar = findViewById(R.id.radar);
        radar.setDuration(2000);
        radar.setStyleIsFILL(true);
        radar.setRadarColor(Color.GRAY);
        radar.start();

        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
        mTvTip.setVisibility(View.INVISIBLE);

    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        // 发送请求建立连接的回应

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

    }


    @Override
    protected void onDestroy() {

        mUnbinder.unbind();
        /**
         * 发送下线广播
         */
        mPeerManager.sendOffLineBroadcast();
        mOsTimer.cancel();
        mPeerManager.stopUdpServer();

        super.onDestroy();
    }


}
