package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.merpyzf.httpcoreserver.ui.HttpServerActivity;
import com.merpyzf.transfermanager.receive.Receiver;
import com.merpyzf.xmshare.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Unbinder mbind;
    @BindView(R.id.btn_start_server)
    Button btnStartServer;
    @BindView(R.id.btn_receive)
    Button btnReceive;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.btn_ss)
    Button btnStartSs;


    private String TAG = MainActivity.class.getSimpleName();
    private Context mContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mbind = ButterKnife.bind(this);

        initEvent();


    }

    private void initEvent() {

        btnStartServer.setOnClickListener(this);
        btnReceive.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnStartSs.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_start_server:
                HttpServerActivity.start(mContext);
                break;
            case R.id.btn_receive:
                ReceiveActivity.start(mContext);
                break;
            case R.id.btn_send:
                SendActivity.start(mContext);
                break;

            case R.id.btn_ss:

                Receiver receiver = new Receiver();
                receiver.startReceive();

                break;

            default:
                break;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}

