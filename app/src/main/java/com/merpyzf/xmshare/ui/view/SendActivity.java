package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.ui.adapter.PeerAdapter;
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 文件发送:
 * <p>
 * 发送端：
 * <p>
 * 发送端也需要建立一个UDPServer，用来获取 接收端的回应
 * <p>
 * 1.当收到对端的回复时，在界面上显示这个可连接的接收端
 * 2.点击接收端设备头像准备建立连接
 * 3.当发送端和接收端确认建立连接时，退出当前页面时发送离线广播，告知对端退出了
 * <p>
 * UDP包的几种请求状态:
 * <p>
 * 上线通知
 * 请求连接
 * 同意建立连接
 * 下线通知
 */
public class SendActivity extends AppCompatActivity {

    private Context mContext;
    private PeerManager mPeerManager;
    private List<Peer> mPeerList = new ArrayList<>();
    private Unbinder mUnbinder;

    @BindView(R.id.rv_peers)
    RecyclerView mRvPeerList;
    private PeerAdapter mPeerAdapter;


    public static void start(Context context) {

        context.startActivity(new Intent(context, SendActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        mContext = this;
        mUnbinder = ButterKnife.bind(this);

        initUI();

        mPeerAdapter = new PeerAdapter(R.layout.item_rv_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);

        String nickName = SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");
        mPeerManager = new PeerManager(this, nickName, new PeerCommunCallback() {
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
                    Toast.makeText(SendActivity.this, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        /**
         * 此处建立的是一个接收UDP信息的服务端
         */
        mPeerManager.listenBroadcast();

    }

    private void initUI() {
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
    }


    @Override
    protected void onDestroy() {
        mUnbinder.unbind();

        // 发送下线广播
        mPeerManager.sendOffLineBroadcast();
        // 关闭UdpServer端，停止接收数据
        mPeerManager.stopUdpServer();

        super.onDestroy();

    }
}
