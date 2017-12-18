package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.util.timer.OSTimer;
import com.merpyzf.xmshare.R;

/**
 * 文件接收,接收的文件肯定来自唯一的一个设备,向局域网内定时发送广播，告知局域网内的设备有接收端的存在
 *
 */
public class ReceiveActivity extends AppCompatActivity {


    private PeerManager mPeerManager;
    private OSTimer mUDPTimer;


    public static void start(Context context) {

        context.startActivity(new Intent(context, ReceiveActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        mPeerManager = new PeerManager(this);
        mPeerManager.listenBroadcast();

        /**
         * 发送上线广播
         */
        mUDPTimer = mPeerManager.sendOnLineBroadcast();


    }

    @Override
    protected void onDestroy() {

        mUDPTimer.cancel();
        /**
         * 发送下线广播
         */
        mPeerManager.sendOffLineBroadcast();
        super.onDestroy();
    }
}
