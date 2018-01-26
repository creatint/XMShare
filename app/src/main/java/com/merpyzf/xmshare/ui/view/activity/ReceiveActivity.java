package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.view.fragment.ReceivePeerFragment;
import com.merpyzf.xmshare.ui.view.fragment.transfer.TransferReceiveFragment;

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

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    private Context mContext;
    private Unbinder mUnbinder;
    private ReceivePeerFragment mReceivePeerFragment;
    private TransferReceiveFragment mTransferReceiveFragment;


    public static void start(Context context) {

        context.startActivity(new Intent(context, ReceiveActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        this.mContext = this;
        mUnbinder = ButterKnife.bind(this);

        initUI();
        initEvent();


    }

    /**
     * 初始化UI
     */
    private void initUI() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("我要接收");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mReceivePeerFragment = new ReceivePeerFragment();
        transaction.replace(R.id.frame_content, mReceivePeerFragment);
        transaction.commit();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        if (mReceivePeerFragment != null) {
            mReceivePeerFragment.setOnReceivePairActionListener(new ReceivePeerFragment.OnReceivePairActionListener() {
                @Override
                public void onRequestSendFileAction() {
                    // 开启一个Socket服务
                    ReceiverManager receiverManager = ReceiverManager.getInstance();
                    new Thread(receiverManager).start();
                    Log.i("w2k", "开启一个ServerScoket等待设备接入");
                }
                @Override
                public void onAgreeSendFileAction() {
                    Log.i("w2k", "同意对端发送文件");
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    mTransferReceiveFragment = new TransferReceiveFragment();
                    transaction.replace(R.id.frame_content,mTransferReceiveFragment);
                    transaction.commit();
                }
            });
        }

    }


    @Override
    public void onBackPressed() {
        if(mTransferReceiveFragment!=null){
            mTransferReceiveFragment.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        // 释放ServerSocket资源
        ReceiverManager.getInstance().release();
        super.onDestroy();
    }
}