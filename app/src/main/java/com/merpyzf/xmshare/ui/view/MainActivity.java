package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.merpyzf.httpcoreserver.ui.HttpServerActivity;
import com.merpyzf.httpcoreserver.util.LogUtil;
import com.merpyzf.transfermanager.XMCommunicate;
import com.merpyzf.xmshare.R;

import java.io.Console;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    private Unbinder mbind;
    @BindView(R.id.btn_start_server)
    Button btnStartServer;
    private String TAG = MainActivity.class.getSimpleName();
    private Context mContext = null;
    private Button mBtnSend = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mbind = ButterKnife.bind(this);
        mBtnSend = findViewById(R.id.btn_send_broadcast);
        final XMCommunicate communicate = new XMCommunicate();


        new Thread(new Runnable() {
            @Override
            public void run() {
                // 接收响应回来的消息
                communicate.receiveUdpMsg(MainActivity.this);
                LogUtil.i(TAG, "接收响应回来的消息");

            }
        }).start();


        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        communicate.sendUdpMsg("Hello");

                    }
                }).start();


            }
        });

        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpServerActivity.start(mContext);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
