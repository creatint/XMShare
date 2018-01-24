package com.merpyzf.xmshare.ui.view.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.send.SenderManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.App;
import com.merpyzf.xmshare.ui.view.fragment.ScanPeerFragment;
import com.merpyzf.xmshare.ui.view.fragment.transfer.TransferSendFragment;

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
public class SendActivity extends AppCompatActivity implements ScanPeerFragment.OnPairActionListener {

    @BindView(R.id.linear_rocket)
    LinearLayout mLinearRocket;
    @BindView(R.id.frame_content)
    FrameLayout mFrameContent;
    @BindView(R.id.tool_bar)
    android.support.v7.widget.Toolbar mToolbar;

    private Context mContext;
    private List<Peer> mPeerList = new ArrayList<>();
    private Unbinder mUnbinder;
    private ScanPeerFragment mScanPeerFragment;
    private TransferSendFragment mTransferSendFragment;
    private PeerManager mPeerManager;
    private SenderManager mSenderManager;


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
        initEvent();


    }


    /**
     * 初始化UI
     */
    private void initUI() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("我要发送");
        // 加载好友扫描的Fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mScanPeerFragment = new ScanPeerFragment();
        transaction.replace(R.id.frame_content, mScanPeerFragment);
        transaction.commit();

    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        mScanPeerFragment.setOnPeerActionListener(this);


    }


    @Override
    public void onSendConnRequestAction() {

        Log.i("w2k", "显示火箭的图标");
        // 设为可见状态
        mLinearRocket.setVisibility(View.VISIBLE);
        mLinearRocket.setFocusable(true);
        mFrameContent.setFocusable(false);
        mFrameContent.setClickable(false);

    }

    @Override
    public void onPeerPairSuccessAction(Peer peer) {


        // 跳转到文件发送的界面
        mSenderManager = SenderManager.getInstance(mContext);
        mSenderManager.send(peer.getHostAddress(), App.getSendFileList());


        // 开始进行文件的发送，并添加动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(mLinearRocket, "translationY", 0.0f - 1000f);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {



            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLinearRocket.setVisibility(View.INVISIBLE);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                mTransferSendFragment = new TransferSendFragment();
                transaction.replace(R.id.frame_content, mTransferSendFragment);
                transaction.commit();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();

    }

    @Override
    public void onPeerPairFailedAction(Peer peer) {
        // 隐藏
        mLinearRocket.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onBackPressed() {
        if(mTransferSendFragment!=null){
            mTransferSendFragment.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        if (mSenderManager != null) {
            mSenderManager.release();
            mSenderManager = null;
        }


        super.onDestroy();
    }

}
