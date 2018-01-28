package com.merpyzf.xmshare.ui.view.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.radarview.RadarLayout;
import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommunCallback;
import com.merpyzf.transfermanager.util.ApManager;
import com.merpyzf.transfermanager.util.NetworkUtil;
import com.merpyzf.transfermanager.util.WifiMgr;
import com.merpyzf.transfermanager.util.timer.OSTimer;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.receiver.APChangedReceiver;
import com.merpyzf.xmshare.ui.adapter.PeerAdapter;
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 我要接收-搜索好友的界面
 * 1 -  局域网内设备发现
 * 2 - AP热点模式
 */
public class ReceivePeerFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {

    private PeerManager mPeerManager;
    private Context mContext;
    private ArrayList<Peer> mPeerList;
    private Unbinder mUnbinder;
    private static final String TAG = ReceivePeerFragment.class.getSimpleName();
    @BindView(R.id.radar)
    RadarLayout radar;
    @BindView(R.id.rv_peers)
    RecyclerView mRvPeerList;
    @BindView(R.id.tv_tip)
    TextView mTvTip;
    // 切换到热点传输模式
    @BindView(R.id.btn_change_ap)
    Button mBtnChangedAp;
    @BindView(R.id.tv_net_name)
    TextView mTvNetName;
    // 网络模式
    @BindView(R.id.tv_mode)
    TextView mTvNetMode;

    private OSTimer mOsTimer;
    private PeerAdapter mPeerAdapter;
    private OnReceivePairActionListener mOnReceivePairActionListener;
    private String mNickName;
    private WifiMgr mWifiMgr;
    private APChangedReceiver mApChangedReceiver;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1;


    public ReceivePeerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_receive_peer, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        this.mContext = getActivity();
        mPeerList = new ArrayList<>();
        initUI();
        initEvent();
        mPeerAdapter = new PeerAdapter(R.layout.item_rv_recive_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);
        mPeerAdapter.setOnItemClickListener(this);

        // 传输的方式
        int transferMode = SharedPreUtils.getInteger(mContext, Constant.SP_USER, Constant.KEY_TRANSFER_MODE,
                Constant.TRANSFER_MODE_LAN);

        if (transferMode == Constant.TRANSFER_MODE_AP) {
            // 热点传输优先-> 无论是否连接wifi都要建立热点
            requestPermissionAndInitAp();
            mBtnChangedAp.setVisibility(View.INVISIBLE);


        } else if (transferMode == Constant.TRANSFER_MODE_LAN) {

            // 局域网传输优先-> 如果没有连接wifi 就开启热点
            mWifiMgr = WifiMgr.getInstance(mContext);

            if (NetworkUtil.isWifi(mContext)) {

                Log.i(TAG, "当前wifi处于WIFI环境");
                initUdpListener();
                mBtnChangedAp.setVisibility(View.VISIBLE);

            } else {

                Toast.makeText(mContext, "建立热点进行传输", Toast.LENGTH_SHORT).show();
                requestPermissionAndInitAp();
                mBtnChangedAp.setVisibility(View.INVISIBLE);
            }

        }

        return rootView;
    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        mBtnChangedAp.setOnClickListener(v -> {

            // 如果当前是网络wifi环境下才可以切换到热点模式进行传输
            if (NetworkUtil.isWifi(mContext)) {

                // 释放UDP局域网内设备发现涉及到的相关资源
                releaseUdpListener();
                // 建立AP
                requestPermissionAndInitAp();
                // 隐藏切换到热点模式按钮
                mBtnChangedAp.setVisibility(View.INVISIBLE);

                Toast.makeText(mContext, "正在拼命开启热点中，请等待...",Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * 初始化一个UDP的监听
     */
    private void initUdpListener() {

        String nickName = com.merpyzf.xmshare.util.SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");
        mPeerManager = new PeerManager(mContext, nickName, new PeerCommunCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {

            }

            @Override
            public void onDeviceOffLine(Peer peer) {

                if (mPeerList.contains(peer)) {

                    mPeerList.remove(peer);
                    Toast.makeText(mContext, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

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

                Toast.makeText(mContext, peer.getNickName() + "请求建立连接", Toast.LENGTH_SHORT).show();

                mPeerAdapter.notifyDataSetChanged();
                // TODO: 2018/1/14  开启Socket服务等待设备连接，并开跳转到文件接收的界面，开始接收文件

                if (mOnReceivePairActionListener != null) {
                    mOnReceivePairActionListener.onRequestSendFileAction();
                }


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


    /**
     * 申请并初始化AP
     */
    private void requestPermissionAndInitAp() {
        // 检查是否具备修改系统设置的权限
        boolean permission = false;
        // 获取当前设备的SDK的版本
        int sdkVersion = Build.VERSION.SDK_INT;
        // 如果
        if (sdkVersion >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(mContext);
        } else {
            permission = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {

            // 拥有权限直接建立热点
            initAp();

        } else {
            // 没有权限则去进行申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // 6.0以上设备的权限申请方式

                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);

            } else {

                // 6.0一下的设备进行权限申请的方式
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_SETTINGS}, REQUEST_CODE_WRITE_SETTINGS);

            }

        }


    }


    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("w2k", "--> onRequestPermissionsResult");
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS && Settings.System.canWrite(mContext)) {
            Log.i("w2k", "权限申请成功");
            initAp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i("w2k", "--> onRequestPermissionsResult");

        if (requestCode == REQUEST_CODE_WRITE_SETTINGS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i("w2k", "权限通过");
            initAp();
        } else {
            Toast.makeText(mContext, "权限被拒绝，无法创建热点", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * 初始化热点
     */
    private void initAp() {

        // 如果热点处于开启状态就将其关闭
        if (ApManager.isApOn(mContext)) {
            ApManager.turnOffAp(mContext);
        }

        // 热点被关闭的回调方法
        mApChangedReceiver = new APChangedReceiver() {
            @Override
            public void onApEnableAction() {

                Toast.makeText(mContext, "Ap初始化成功", Toast.LENGTH_SHORT).show();
                //
                String apSSID = ApManager.getApSSID(mContext);
                mTvNetName.setText(apSSID);

                if (mOnReceivePairActionListener != null) {
                    mOnReceivePairActionListener.onApEnableAction();
                }

            }


            @Override
            public void onApDisAbleAction() {

                // 热点被关闭的回调方法

            }
        };


        IntentFilter intentFilter = new IntentFilter(APChangedReceiver.ACTION_WIFI_AP_STATE_CHANGED);
        mContext.registerReceiver(mApChangedReceiver, intentFilter);
        // 设置一个昵称
        String nickName = "macbook";
        int avatar = 1;
        // 开启一个热点
        ApManager.configApState(mContext, nickName, avatar);

    }


    private void checkIsHide() {
        if (mTvTip == null) return;

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

        if (NetworkUtil.isWifi(mContext)) {

            WifiInfo currConnWifiInfo = WifiMgr.getInstance(mContext).getCurrConnWifiInfo();
            String ssid = currConnWifiInfo.getSSID();
            mTvNetName.setText(ssid);
            mTvNetMode.setVisibility(View.VISIBLE);

            new OSTimer(null, () -> {
                getActivity().runOnUiThread(() -> {

                    ObjectAnimator animator = ObjectAnimator.ofFloat(mTvNetMode, "alpha", 1f, 0f);
                    animator.setDuration(1000);//时间1s
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            mTvNetMode.setVisibility(View.INVISIBLE);

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                });

            },3000, false).start();
        }else {

            mTvNetMode.setVisibility(View.INVISIBLE);
        }


        radar.setDuration(2000);
        radar.setStyleIsFILL(false);
        radar.setRadarColor(Color.WHITE);
        radar.start();

        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
        mTvTip.setVisibility(View.INVISIBLE);

    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        // 发送同意对端发送文件的回应
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
    public void onDestroy() {
        mUnbinder.unbind();

        releaseUdpListener();
        // 释放ServerSocket资源
//        ReceiverManager.getInstance().release();
        super.onDestroy();
    }

    /**
     * 释放UdpServer占用的资源
     */
    private void releaseUdpListener() {

        if (mPeerManager != null) {
            /**
             * 发送下线广播
             */
            mPeerManager.sendOffLineBroadcast();
            mPeerManager = null;
        }

        if (mOsTimer != null) {
            mOsTimer.cancel();
            mOsTimer = null;
        }
        if (mPeerManager != null) {
            mPeerManager.stopUdpServer();
            mPeerManager = null;
        }

    }

    public interface OnReceivePairActionListener {

        /**
         * 请求发送文件(提前建立ServerSocket)
         */
        void onRequestSendFileAction();

        /**
         * AP建立成功的回调
         */
        void onApEnableAction();


    }

    public void setOnReceivePairActionListener(OnReceivePairActionListener onReceivePairActionListener) {
        this.mOnReceivePairActionListener = onReceivePairActionListener;
    }
}
