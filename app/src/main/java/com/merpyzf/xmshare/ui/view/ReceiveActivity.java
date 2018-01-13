package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.merpyzf.radarview.RadarLayout;
import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;

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
public class ReceiveActivity extends AppCompatActivity {


    private PeerManager mPeerManager;
    private Context mContext;
    private ArrayList<Peer> mPeerList;
    private RadarLayout radar;
    private Unbinder mUnbinder;
    @BindView(R.id.rv_peers)
    RecyclerView mRvPeers;

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


        String nickName = com.merpyzf.xmshare.util.SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");

        mPeerManager = new PeerManager(this, nickName, new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {
                if (!mPeerList.contains(peer)) {

                    mPeerList.add(peer);
                    Log.i("w2k", "有设备上线了:" + peer.getNickName() + " " + peer.getHostAddress());

                }

            }

            @Override
            public void onDeviceOffLine(Peer peer) {

                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    Toast.makeText(ReceiveActivity.this, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onRequestConnect(Peer peer) {

                Log.i("w2k", "有设备请求建立连接:" + peer.getNickName() + " " + peer.getHostAddress());

                Toast.makeText(ReceiveActivity.this, peer.getNickName()+"请求建立连接", Toast.LENGTH_SHORT).show();


            }
        });

        mPeerManager.listenBroadcast();

        /**
         * 循环间隔一段时间发送一个上线广播
         */
        mPeerManager.sendOnLineBroadcast(true);



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

        mRvPeers.setLayoutManager(new LinearLayoutManager(mContext));
    }




    @Override
    protected void onDestroy() {

        mUnbinder.unbind();
        /**
         * 发送下线广播
         */
        mPeerManager.sendOffLineBroadcast();
        mPeerManager.stopUdpServer();

        super.onDestroy();
    }
}
