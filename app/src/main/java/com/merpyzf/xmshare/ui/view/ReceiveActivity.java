package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;

import java.util.ArrayList;

/**
 * 文件接收,接收的文件肯定来自唯一的一个设备,向局域网内定时发送广播，告知局域网内的设备有接收端的存在
 */
public class ReceiveActivity extends AppCompatActivity {


    private PeerManager mPeerManager;
    private Context mContext;
    private ArrayList<Peer> mPeerList;
    private TextView tv_show_device;

    public static void start(Context context) {

        context.startActivity(new Intent(context, ReceiveActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        this.mContext = this;
        mPeerList = new ArrayList<>();
        tv_show_device = findViewById(R.id.tv_show_peer);


        String nickName = com.merpyzf.xmshare.util.SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");
        mPeerManager = new PeerManager(this, nickName);

        mPeerManager.listenBroadcast();

        /**
         * 循环间隔一段时间发送一个上线广播
         */
        mPeerManager.sendOnLineBroadcast();

        mPeerManager.setOnPeerCallback(new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {

                if (!mPeerList.contains(peer)) {

                    mPeerList.add(peer);
                    updateShowContent();

                    Log.i("w2k", "有设备上线了:" + peer.getNickName() + " " + peer.getHostAddress());

                }


            }

            @Override
            public void onDeviceOffLine(Peer peer) {

                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    updateShowContent();
                    Toast.makeText(ReceiveActivity.this, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

                }


            }
        });

    }


    /**
     * 更新界面内容显示
     */
    private void updateShowContent() {


        StringBuffer sb = new StringBuffer();

        if (mPeerList.size() > 0) {
            for (Peer peer : mPeerList) {

                sb.append("主机地址: " + peer.getHostAddress() + "\n" + "昵称:  " + peer.getNickName() + "\n");
                sb.append("*********************************");
                sb.append("\n");

            }
        } else {

            sb.append("当前局域网内无可连接设备");
        }


        tv_show_device.setText(sb.toString());


    }

    @Override
    protected void onDestroy() {


        /**
         * 发送下线广播
         */
        mPeerManager.sendOffLineBroadcast();
        mPeerManager.stopUdpServer();

        super.onDestroy();
    }
}
