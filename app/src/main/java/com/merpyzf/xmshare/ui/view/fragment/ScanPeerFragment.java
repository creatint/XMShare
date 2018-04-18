package com.merpyzf.xmshare.ui.view.fragment;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.httpcoreserver.util.NetworkUtil;
import com.merpyzf.transfermanager.PeerManager;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.Peer;
import com.merpyzf.transfermanager.entity.SignMessage;
import com.merpyzf.transfermanager.interfaces.PeerCommCallback;
import com.merpyzf.transfermanager.util.ApManager;
import com.merpyzf.transfermanager.util.WifiMgr;
import com.merpyzf.transfermanager.util.timer.OSTimer;
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Const;
import com.merpyzf.xmshare.ui.adapter.PeerAdapter;
import com.merpyzf.xmshare.ui.view.activity.InputHotspotPwdActivity;
import com.merpyzf.xmshare.util.SharedPreUtils;
import com.merpyzf.xmshare.util.ToastUtils;
import com.merpyzf.xmshare.util.UiUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 扫描附近的设备
 */
public class ScanPeerFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {
    private Context mContext;
    private Unbinder mUnbinder;
    private PeerManager mPeerManager;
    private List<Peer> mPeerList = new ArrayList<>();
    @BindView(R.id.rv_peers)
    RecyclerView mRvPeerList;
    @BindView(R.id.tv_tip)
    TextView mTvTip;

    private PeerAdapter mPeerAdapter;
    private OnPairActionListener mOnPairActionListener;

    // 要建立连接的对端设备
    private Peer mPeerRequestConn;
    private WifiMgr mWifiMgr;
    private String mLocalAddress;
    private ScanPeerHandler mHandler;
    // 扫描WIFI
    private static final int TYPE_SCAN_WIFI = 1;
    // 发送文件
    private static final int TYPE_SEND_FILE = 2;

    private static final int TYPE_GET_IP = 3;

    // 获取接收端ip失败
    private static final int TYPE_GET_IP_FAILED = 4;

    private int mCountPing = 10;


    // 用于定时扫描wifi的定时器
    private OSTimer mScanWifiTimer;
    // 是否停止扫描
    private boolean isStopScan = false;

    private static final String TAG = ScanPeerFragment.class.getSimpleName();

    public ScanPeerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scan_peer, container, false);
        mContext = getActivity();
        mUnbinder = ButterKnife.bind(this, rootView);

        init();
        initUI();

        // 开启一个udp server 用于和局域网内的设备进行交互
        mPeerManager = new PeerManager(mContext, App.getNickname(), new PeerCommCallback() {
            @Override
            public void onDeviceOnLine(Peer peer) {
                if (!mPeerList.contains(peer)) {
                    mPeerList.add(peer);
                    mPeerAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onDeviceOffLine(Peer peer) {
                if (mPeerList.contains(peer)) {
                    mPeerList.remove(peer);
                    mPeerAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onRequestConnect(Peer peer) {

            }

            @Override
            public void onAnswerRequestConnect(Peer peer) {
                // 回应对端申请的建立连接的请求
                if (peer.equals(mPeerRequestConn)) {
                    Toast.makeText(mContext, "验证成功,开始建立连接", Toast.LENGTH_SHORT).show();
                    // TODO: 2018/1/14 在这边开始建立Socket连接，并发送文件，切换到文件传输的界面
                    if (mOnPairActionListener != null) {
                        mOnPairActionListener.onPeerPairSuccessAction(peer);
                    }
                } else {
                    if (mOnPairActionListener != null) {
                        mOnPairActionListener.onPeerPairFailedAction(peer);
                    }
                    Toast.makeText(mContext, "Peer不匹配，验证失败", Toast.LENGTH_SHORT).show();

                }
                // 将界面切换到文件传输的Fragment，根据peer中的主机地址连接到指定的那个主机
            }

        });
        mPeerManager.listenBroadcast();


        // TODO: 2018/4/18 申请获取位置信息的权限

        new RxPermissions(getActivity())
                .requestEach(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(permission -> {

                    if(permission.granted){

                        ToastUtils.showShort(getActivity(),"位置权限被授予");

                        new OSTimer(null, () -> mHandler.sendEmptyMessage(TYPE_SCAN_WIFI), 0, false).start();
                        mScanWifiTimer = new OSTimer(null, () -> mHandler.sendEmptyMessage(TYPE_SCAN_WIFI), 5000, true);
                        mScanWifiTimer.start();


                    }else {

                        ToastUtils.showShort(mContext, "请授予位置权限，否则无法扫描附近的热点！");

                    }

                });

        return rootView;
    }

    private void init() {

        // 关闭热点开启wifi
        if (ApManager.isApOn(mContext)) {
            ApManager.turnOffAp(mContext);
        }
        mWifiMgr = WifiMgr.getInstance(mContext);
        if (!mWifiMgr.isWifiEnable()) {
            mWifiMgr.openWifi();

        }


        mHandler = new ScanPeerHandler(this);

    }

    /**
     * 初始化UI
     */
    private void initUI() {

        mTvTip.setTextColor(Color.WHITE);
        mTvTip.setText("正在扫描周围的接收者...");

        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
        mPeerAdapter = new PeerAdapter(R.layout.item_rv_send_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);
        mPeerAdapter.setOnItemClickListener(this);


    }

    /**
     * RecyclerView 列表中item点击的回调事件
     *
     * @param adapter
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {


        if (UiUtils.clickValid()) {

            Peer peer = (Peer) adapter.getItem(position);

            if (peer == null) {
                return;
            }

            // 用户点击的是开启热点的用户
            if (peer.isHotsPot()) {

                if (peer.isAndroidODevice(peer.getSsid())) {


                    Intent intent = new Intent(getContext(), InputHotspotPwdActivity.class);
                    intent.putExtra("ssid", peer.getSsid());
                    startActivityForResult(intent, 1);


                } else {
                    mTvTip.setTextColor(Color.WHITE);
                    mTvTip.setText("正在努力连接到该网络...");
                    connectWifiAndTransfer(peer, null);
                    isStopScan = true;
                }

            } else {
                // 点击的是局域网内的用户
                // 需要在peer上加一个标记
                mPeerRequestConn = peer;
                // 构造UDP消息的内容
                SignMessage signMessage = new SignMessage();
                signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
                signMessage.setNickName(SharedPreUtils.getNickName(mContext));
                signMessage.setAvatarPosition(SharedPreUtils.getAvatar(mContext));
                signMessage.setMsgContent(" ");
                signMessage.setCmd(SignMessage.cmd.REQUEST_CONN);
                String msg = signMessage.convertProtocolStr();


                InetAddress dest = null;
                try {
                    dest = InetAddress.getByName(mPeerRequestConn.getHostAddress());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                // 将消息发送给对端
                mPeerManager.send2Peer(msg, dest, com.merpyzf.transfermanager.common.Const.UDP_PORT);
                if (mOnPairActionListener != null) {
                    mOnPairActionListener.onSendConnRequestAction();
                }
                Toast.makeText(mContext, "发送建立请求连接", Toast.LENGTH_SHORT).show();
            }

        } else {


            Log.i("wk", "你点击的太快了");

        }
    }


    /**
     * 连接热点并传输文件
     *
     * @param peer
     * @param pwd  null: 表示要连接的热点密码为空
     */
    public void connectWifiAndTransfer(Peer peer, String pwd) {

        // TODO: 2018/4/18 创建热点的时候提醒用户是否需要关闭移动数据
        WifiConfiguration wifiCfg = null;
        if (null == pwd) {
            wifiCfg = WifiMgr.createWifiCfg(peer.getSsid(), null, WifiMgr.WIFICIPHER_NOPASS);
        } else {
            wifiCfg = WifiMgr.createWifiCfg(peer.getSsid(), pwd, WifiMgr.WIFICIPHER_WPA);
        }

//        连接热点
        mWifiMgr.connectNewWifi(wifiCfg);

        // 获取远端建立热点设备的ip地址
        String ipAddressFromHotspot = WifiMgr.getInstance(mContext).getIpAddressFromHotspot();

        Log.i("w2k", "尝试第一次获取接收端的IP地址: " + ipAddressFromHotspot);
        mLocalAddress = WifiMgr.getInstance(mContext).getIpAddressFromHotspot();

        App.getSingleThreadPool().execute(() -> {
            // 当连接上wifi后立即获取对端主机地址，有可能获取不到，需要多次获取才能拿到
            int count = 0;
            while (count < 40) {

                mLocalAddress = WifiMgr.getInstance(mContext).getIpAddressFromHotspot();

                Message msg = mHandler.obtainMessage();
                msg.what = TYPE_GET_IP;
                msg.arg1 = count;
                mHandler.sendMessage(msg);


                Log.i(TAG, "第 " + count + " 次尝试获取接收端IP 获取结果->  " + mLocalAddress);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 获取到主机地址，发送消息到Handler进行文件的发送
                if (!mLocalAddress.equals("0.0.0.0")) {

                    // 设置主机地址
                    peer.setHostAddress(mLocalAddress);
                    Message message = mHandler.obtainMessage();
                    message.obj = peer;
                    message.what = TYPE_SEND_FILE;
                    mHandler.sendMessage(message);
                    break;
                }

                count++;
            }

            // 没有获取到接收端的ip
            if (mLocalAddress.equals("0.0.0.0")) {
                mHandler.sendEmptyMessage(TYPE_GET_IP_FAILED);
            }


            Log.i("wk", "receiver get local Ip ----->>>" + mLocalAddress);

        });

    }

    /**
     * 扫描WIFI
     */
    public void scanWifi() {

        List<ScanResult> scanResults = mWifiMgr.startScan();
        WifiConfiguration configuration;
        if (scanResults == null) {
            return;
        }
        // 扫描之前先移除上一次扫描到的热点信号
        for (int i = 0; i < mPeerList.size(); i++) {
            if (mPeerList.get(i).isHotsPot()) {
                mPeerList.remove(i);
            }
        }
        for (ScanResult scanResult : scanResults) {

            if (scanResult.SSID.startsWith(com.merpyzf.transfermanager.common.Const.HOTSPOT_PREFIX_IDENT) || scanResult.SSID.startsWith(com.merpyzf.transfermanager.common.Const.HOTSPOT_PREFIX_IDENT_O)) {
                String nick = null;
                int avatarPosition = 0;
                if (scanResult.SSID.startsWith(com.merpyzf.transfermanager.common.Const.HOTSPOT_PREFIX_IDENT)) {
                    String[] apNickAndAvatar = com.merpyzf.transfermanager.util.NetworkUtil.getApNickAndAvatar(scanResult.SSID);
                    avatarPosition = Integer.valueOf(apNickAndAvatar[0]);
                    nick = apNickAndAvatar[1];
                } else if (scanResult.SSID.startsWith(com.merpyzf.transfermanager.common.Const.HOTSPOT_PREFIX_IDENT_O)) {
                    nick = scanResult.SSID;
                    avatarPosition = Const.AVATAR_LIST.size() - 1;
                }
                Peer peer = new Peer();
                peer.setHostAddress("未知");
                peer.setSsid(scanResult.SSID);
                peer.setAvatarPosition(avatarPosition);
                peer.setNickName(nick);
                peer.setHotsPot(true);

                mPeerList.add(peer);
            }
        }
        mPeerAdapter.notifyDataSetChanged();
    }


    private static class ScanPeerHandler extends Handler {

        private final WeakReference<ScanPeerFragment> mFragment;

        public ScanPeerHandler(ScanPeerFragment fragment) {

            mFragment = new WeakReference<ScanPeerFragment>(fragment);


        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ScanPeerFragment scanPeerFragment = mFragment.get();
            switch (msg.what) {


                case TYPE_SCAN_WIFI:
                    if (!scanPeerFragment.isStopScan) {
                        scanPeerFragment.scanWifi();
                    }
                    break;

                case TYPE_SEND_FILE:

                    Peer peer = (Peer) msg.obj;
                    Log.i("w2k", "准备向 " + peer.getHostAddress() + " 发送文件");
                    // 当前ping的次数
                    final int[] currentPingCount = {0};

                    Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                    Log.i(TAG, "ping.....");

                                    if (currentPingCount[0] < scanPeerFragment.mCountPing) {

                                        scanPeerFragment.mTvTip.setTextColor(Color.WHITE);
                                        scanPeerFragment.mTvTip.setText("正在检查网络连通性...");

                                        if (com.merpyzf.transfermanager.util.NetworkUtil.pingIpAddress(peer.getHostAddress())) {
                                            if (scanPeerFragment.mOnPairActionListener != null) {
                                                // 取消wifi扫描
                                                scanPeerFragment.isStopScan = true;
                                                scanPeerFragment.mOnPairActionListener.onSendToHotspotAction(peer, App.getSendFileList());
                                                d.dispose();
                                            }
                                        } else {
                                            scanPeerFragment.isStopScan = false;
                                        }
                                    } else {
                                        d.dispose();
                                        // 继续开始扫描附近wifi
                                        scanPeerFragment.isStopScan = false;

                                    }

                                    currentPingCount[0]++;

                                }

                                @Override
                                public void onNext(Long value) {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });


                    break;

                case TYPE_GET_IP_FAILED:
                    scanPeerFragment.isStopScan = false;
                    scanPeerFragment.mTvTip.setTextColor(Color.RED);
                    scanPeerFragment.mTvTip.setText("获取接收端IP地址失败，请点击好友头像重试...");

                    break;

                case TYPE_GET_IP:

                    int count = msg.arg1;
                    scanPeerFragment.mTvTip.setText("正在第" + count + "次尝试获取接收端IP地址...");

                    break;

                default:
                    break;
            }

        }

    }


    public interface OnPairActionListener {

        /**
         * 发送连接的请求
         */
        void onSendConnRequestAction();

        /**
         * 配对成功
         *
         * @param peer
         */
        void onPeerPairSuccessAction(Peer peer);

        /**
         * 配对失败
         *
         * @param peer
         */
        void onPeerPairFailedAction(Peer peer);

        /**
         * 向接收端发送文件
         *
         * @param peer        对端的主机信息
         * @param fileInfoLis 待发送的文件列表
         */
        void onSendToHotspotAction(Peer peer, List<FileInfo> fileInfoLis);


    }

    public void setOnPeerActionListener(OnPairActionListener onPairActionListener) {
        this.mOnPairActionListener = onPairActionListener;
    }

    /**
     * 获取从上一个Activity拿到的用户名和密码，并与热点建立连接
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == InputHotspotPwdActivity.RESULT_GET_HOTSPOT_INFO) {
            String hotspot_info = data.getStringExtra("hotspot_info");
            ToastUtils.showShort(getContext(), hotspot_info);
            Peer peer = new Peer();
            String preSharedKey = null;
            try {

                JSONObject jsonObject = new JSONObject(hotspot_info);
                peer.setAvatarPosition(Const.AVATAR_LIST.size() - 1);
                peer.setHotsPot(true);
                peer.setNickName((String) jsonObject.get("ssid"));
                peer.setSsid((String) jsonObject.get("ssid"));
                preSharedKey = (String) jsonObject.get("preSharedKey");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 连接热点
            connectWifiAndTransfer(peer, preSharedKey);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
        // 发送下线广播
        mPeerManager.sendOffLineBroadcast();
        // 关闭UdpServer端，停止接收数据
        mPeerManager.stopUdpServer();
        mHandler.removeCallbacks(null);
        super.onDestroy();
    }
}
