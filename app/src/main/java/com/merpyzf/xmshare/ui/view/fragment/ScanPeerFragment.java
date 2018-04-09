package com.merpyzf.xmshare.ui.view.fragment;


import android.content.Context;
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
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.ui.adapter.PeerAdapter;
import com.merpyzf.xmshare.util.SharedPreUtils;

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
    private MyHandler mHandler;
    // 扫描WIFI
    private static final int TYPE_SCAN_WIFI = 1;
    // 发送文件
    private static final int TYPE_SEND_FILE = 2;

    private static final int TYPE_GET_IP = 3;

    // 获取接收端ip失败
    private static final int TYPE_GET_IP_FAILED = 4;

    private int mCountPing = 10;

    private static final String TAG = ScanPeerFragment.class.getSimpleName();
    // 用于定时扫描wifi的定时器
    private OSTimer mScanWifiTimer;
    // 是否停止扫描
    private boolean isStopScan = false;



    public ScanPeerFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scan_peer, container, false);
        mContext = getActivity();
        mUnbinder = ButterKnife.bind(this, rootView);

        initUI();
        mPeerAdapter = new PeerAdapter(R.layout.item_rv_send_peer, mPeerList);
        mRvPeerList.setAdapter(mPeerAdapter);
        mPeerAdapter.setOnItemClickListener(this);
        mWifiMgr = WifiMgr.getInstance(mContext);
        mHandler = new MyHandler();

        // 如果热点开启将将其关闭
        if (ApManager.isApOn(mContext)) {
            ApManager.turnOffAp(mContext);
        }


        // 如果wifi关闭就去开启wifi
        if (!mWifiMgr.isWifiEnable()) {
            mWifiMgr.openWifi();

        }


        String nickName = SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");
        // 这里只需要建立一次
        mPeerManager = new PeerManager(mContext, nickName, new PeerCommCallback() {
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
                    Toast.makeText(mContext, "【接收文件】设备离线了 --> " + peer.getHostAddress(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onRequestConnect(Peer peer) {

            }


            @Override
            public void onAnswerRequestConnect(Peer peer) {

                Log.i("wk", "onAnswerRequestConnect方法执行\n peer: " + peer.getHostAddress());

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
        /**
         * 此处建立的是一个接收UDP信息的服务端
         */
        mPeerManager.listenBroadcast();


        new OSTimer(null, () -> mHandler.sendEmptyMessage(TYPE_SCAN_WIFI), 0, false).start();
        mScanWifiTimer = new OSTimer(null, () -> mHandler.sendEmptyMessage(TYPE_SCAN_WIFI), 5000, true);
        mScanWifiTimer.start();

        return rootView;
    }

    /**
     * 初始化UI
     */
    private void initUI() {

        mTvTip.setTextColor(Color.WHITE);
        mTvTip.setText("正在扫描周围的接收者...");
        mRvPeerList.setLayoutManager(new LinearLayoutManager(mContext));
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


        Peer peer = (Peer) adapter.getItem(position);

        // 用户点击的是开启热点的用户
        if (peer.isHotsPot()) {

            mTvTip.setTextColor(Color.WHITE);
            mTvTip.setText("正在努力连接到该网络...");
            // 连接并传输wifi
            connectWifiAndTransfer(peer);
            isStopScan = true;



        } else {
            // 点击的是局域网内的用户
            // 需要在peer上加一个标记
            mPeerRequestConn = peer;
            // 构造UDP消息的内容
            SignMessage signMessage = new SignMessage();
            signMessage.setHostAddress(NetworkUtil.getLocalIp(mContext));
            signMessage.setNickName(SharedPreUtils.getNickName(mContext));
            signMessage.setAvatarPosition(SharedPreUtils.getAvatar(mContext));
            signMessage.setMsgContent("建立连接请求");
            signMessage.setCmd(SignMessage.cmd.REQUEST_CONN);
            String msg = signMessage.convertProtocolStr();


            InetAddress dest = null;
            try {
                dest = InetAddress.getByName(mPeerRequestConn.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            // 将消息发送给对端
            mPeerManager.send2Peer(msg, dest, com.merpyzf.transfermanager.constant.Constant.UDP_PORT);
            if (mOnPairActionListener != null) {
                mOnPairActionListener.onSendConnRequestAction();
            }
            Toast.makeText(mContext, "发送建立请求连接", Toast.LENGTH_SHORT).show();

            // 在屏幕中间显示一个火箭的图标
            // 将这个事件回调给Activity
        }


    }


    /**
     * 连接wifi并传输文件
     *
     * @param peer
     */
    public void connectWifiAndTransfer(Peer peer) {


        if (peer.getSsid().contains("XM")) {

            Log.i("w2k", "连接wifi " + peer.getSsid());
            WifiConfiguration wifiCfg = WifiMgr.createWifiCfg(peer.getSsid(), null, WifiMgr.WIFICIPHER_NOPASS);
            // 连接没有密码的热点
            mWifiMgr.connectNewWifi(wifiCfg);
            // 获取远端建立热点设备的ip地址
            String ipAddressFromHotspot = WifiMgr.getInstance(mContext).getIpAddressFromHotspot();

            Log.i("w2k", "尝试第一次获取接收端的IP地址: " + ipAddressFromHotspot);
            mLocalAddress = WifiMgr.getInstance(mContext).getIpAddressFromHotspot();

            App.getSingleThreadPool().execute(() -> {
                // 当连接上wifi后立即获取对端主机地址，有可能获取不到，需要多次获取才能拿到
                int count = 0;
                while (count < 10) {

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


                Log.i("w2k", "receiver get local Ip ----->>>" + mLocalAddress);

            });


        }
    }

    /**
     * 扫描WIFI
     */
    public void scanWifi() {

        mWifiMgr.startScan();

        List<ScanResult> scanResults = mWifiMgr.getScanResults();

        if (scanResults == null) {
            return;
        }

        // 扫描之前先移除上一次扫描到的热点信号
        for (int i = 0; i < mPeerList.size(); i++) {
            if (mPeerList.get(i).isHotsPot()) {
                mPeerList.remove(i);
            }
        }

        Log.i("w2k", "扫描附近的wifi");

        for (ScanResult scanResult : scanResults) {

            Log.i("w2k", "wifi 名称-> "+scanResult.SSID);

            if (scanResult.SSID.startsWith("XM")) {

                Log.i("w2k", "符合要求的WIFI SSID -> " + scanResult.SSID);
                String[] apNickAndAvatar = com.merpyzf.transfermanager.util.NetworkUtil.getApNickAndAvatar(scanResult.SSID);

                Peer peer = new Peer();
                peer.setHostAddress("未知");
                peer.setSsid(scanResult.SSID);
                peer.setAvatarPosition(Integer.valueOf(apNickAndAvatar[0]));
                // 设置用户名
                peer.setNickName(apNickAndAvatar[1]);
                peer.setHotsPot(true);

                mPeerList.add(peer);

                Log.i("w2k", "头像 -> " + apNickAndAvatar[0]);
                Log.i("w2k", "用户名 -> " + apNickAndAvatar[1]);
            }
        }
        mPeerAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
        // 发送下线广播
        mPeerManager.sendOffLineBroadcast();
        // 关闭UdpServer端，停止接收数据
        mPeerManager.stopUdpServer();
        super.onDestroy();
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


    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case TYPE_SCAN_WIFI:
                    if(!isStopScan) {
                        scanWifi();
                    }
                    break;

                case TYPE_SEND_FILE:

                    Peer peer = (Peer) msg.obj;
                    Log.i("w2k", "向 " + peer.getHostAddress() + " 发送文件");


                    // 当前ping的次数
                    final int[] currentPingCount = {0};

                    Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                    Log.i(TAG, "ping.....");

                                    if (currentPingCount[0] < mCountPing) {

                                        mTvTip.setTextColor(Color.WHITE);
                                        mTvTip.setText("正在检查网络连通性...");

                                        if (com.merpyzf.transfermanager.util.NetworkUtil.pingIpAddress(peer.getHostAddress())) {
                                            if (mOnPairActionListener != null) {
                                                // 取消wifi扫描
//                                                mScanWifiTimer.cancel();
                                               isStopScan = true;
                                                mOnPairActionListener.onSendToHotspotAction(peer, App.getSendFileList());
                                                d.dispose();
                                            }
                                        }
                                    } else {
                                        d.dispose();
                                        // 继续开始扫描附近wifi
                                        isStopScan = false;

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
                    isStopScan = false;
                    mTvTip.setTextColor(Color.RED);
                    mTvTip.setText("获取接收端IP地址失败，请点击好友头像重试...");

                    break;

                case TYPE_GET_IP:

                    int count = msg.arg1;
                    mTvTip.setText("正在第" + count + "次尝试获取接收端IP地址...");

                    break;

                default:
                    break;
            }

        }

    }
}
