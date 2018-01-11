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
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 文件发送:
 * <p>
 * 文件发送需要扫描局域网内所有可以连接的设备，将可连接的所有设备显示在界面，并选择需要进行文件发送的那个设备
 * 定时向局域网内发送组播数据
 */
public class SendActivity extends AppCompatActivity {

    private Context mContext;
    private PeerManager mPeerManager;
    private List<Peer> mPeerList = new ArrayList<>();
    private Unbinder mUnbinder;
    @BindView(R.id.tv_show_device)
    TextView tv_show_device;

    public static void start(Context context) {

        context.startActivity(new Intent(context, SendActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        mContext = this;

        mUnbinder = ButterKnife.bind(this);

        String nickName = SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");

        mPeerManager = new PeerManager(this, nickName);

        /**
         * 此处建立的是一个接收UDP信息的服务端
         */
        mPeerManager.listenBroadcast();


        mPeerManager.setOnPeerCallback(new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {

                if (!mPeerList.contains(peer)) {

                    mPeerList.add(peer);

                    Log.i("w2k", "有新设备上线了: " + peer.getNickName());

                    updateShowContent();
                }


            }

            @Override
            public void onDeviceOffLine(Peer peer) {

                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    updateShowContent();
                    Log.i("w2k", "设备离线了");
                    Toast.makeText(SendActivity.this, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

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

        if (tv_show_device != null) {
            tv_show_device.setText(sb.toString());
        }
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
