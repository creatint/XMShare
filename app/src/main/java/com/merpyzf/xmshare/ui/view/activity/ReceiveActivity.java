package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.interfaces.TransferObserver;
import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.transfermanager.util.ApManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.view.fragment.ReceivePeerFragment;
import com.merpyzf.xmshare.ui.view.fragment.transfer.TransferReceiveFragment;
import com.merpyzf.xmshare.util.SharedPreUtils;

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
    private PeerManager mPeerManager;
    // 标记当前是否正在进行文件传输
    private boolean isTransfering = false;
    private static final String TAG = ReceiveActivity.class.getSimpleName();

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

        String mNickName = SharedPreUtils.getNickName(mContext);
        mPeerManager = new PeerManager(mContext, mNickName, null);
        mPeerManager.setPeerTransferBreakListener(peer -> {

            Log.i(TAG, "收到中断的广播了");

            if (isTransfering) {
                Toast.makeText(mContext, "对端 " + peer.getNickName() + "退出了，即将关闭", Toast.LENGTH_SHORT).show();
                finish();
            }

        });
        // 开启一个UDPServer
        mPeerManager.listenBroadcast();

    }


    /**
     * 初始化UI
     */
    private void initUI() {


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("我要接收");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // 扫描附近待发送文件的设备
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
                    receiverManager.register(new TransferObserver() {
                        @Override
                        public void onTransferProgress(FileInfo fileInfo) {

                        }

                        // TODO: 2018/1/28 增加一个文件全部传输完毕的回调
                        @Override
                        public void onTransferStatus(FileInfo fileInfo) {
                            // 如果当前传输的是最后一个文件，并且传输成功后重置标记
                            if (fileInfo.getIsLast() == 1 && fileInfo.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_SUCCESS) {
                                isTransfering = false;
                            }
                        }
                    });
                    new Thread(receiverManager).start();
                    Log.i("w2k", "开启一个ServerScoket等待设备接入");
                    receiverManager.setOnTransferFileListListener(transferFileList -> {

                        Log.i("w2k", "同意对端发送文件");
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        mTransferReceiveFragment = new TransferReceiveFragment(transferFileList);
                        transaction.replace(R.id.frame_content, mTransferReceiveFragment);
                        transaction.commit();
                        isTransfering = true;


                    });


                }

                @Override
                public void onApEnableAction() {

                    ReceiverManager receiverManager = ReceiverManager.getInstance();
                    new Thread(receiverManager).start();
                    // 监听待传输的文件列表是否发送成功
                    receiverManager.setOnTransferFileListListener(transferFileList -> {

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        mTransferReceiveFragment = new TransferReceiveFragment(transferFileList);
                        transaction.replace(R.id.frame_content, mTransferReceiveFragment);
                        transaction.commit();
                        isTransfering = true;
                    });
                }
            });
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 发送传输中断的广播
        mPeerManager.sendTransferBreakBroadcast();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();

        if (ApManager.isApOn(mContext)) {
            ApManager.turnOffAp(mContext);
        }

        // 释放ServerSocket资源
        ReceiverManager.getInstance().release();


        super.onDestroy();
    }
}