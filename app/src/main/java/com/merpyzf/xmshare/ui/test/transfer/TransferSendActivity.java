package com.merpyzf.xmshare.ui.test.transfer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.merpyzf.transfermanager.send.Sender;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.App;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TransferSendActivity extends AppCompatActivity {


    @BindView(R.id.btn_send)
    Button btnSend;

    private Unbinder mUnbinder;


    public static void start(Context context) {

        context.startActivity(new Intent(context, TransferSendActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sned);
        mUnbinder = ButterKnife.bind(this);


        Sender sender = new Sender();

        // 发送文件
        btnSend.setOnClickListener(v -> {

            sender.send(App.getSendFileList());

        });

    }


}
