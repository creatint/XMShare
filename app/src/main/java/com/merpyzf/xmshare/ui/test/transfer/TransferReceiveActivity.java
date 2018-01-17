package com.merpyzf.xmshare.ui.test.transfer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.merpyzf.transfermanager.receive.Receiver;
import com.merpyzf.xmshare.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TransferReceiveActivity extends AppCompatActivity {

    private Unbinder mUnbinder;

    public static void start(Context context) {

        context.startActivity(new Intent(context, TransferReceiveActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive2);
        mUnbinder = ButterKnife.bind(this);


        Receiver receiver = new Receiver();
        receiver.start();


    }


}
