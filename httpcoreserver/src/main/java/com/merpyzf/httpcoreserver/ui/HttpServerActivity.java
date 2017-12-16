package com.merpyzf.httpcoreserver.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.merpyzf.httpcoreserver.R;
import com.merpyzf.httpcoreserver.constant.Constant;
import com.merpyzf.httpcoreserver.service.WebService;
import com.merpyzf.httpcoreserver.util.NetworkUtil;


public class HttpServerActivity extends AppCompatActivity {
    private TextView tvIpAddress;
    private Button btnStart;
    private Intent mServicIentent;


    public static void start(Context context) {

        context.startActivity(new Intent(context, HttpServerActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);
        tvIpAddress = findViewById(R.id.tv_ip_address);
        btnStart = findViewById(R.id.btn_start);

        String ip = NetworkUtil.getLocalIp(this);
        Constant.IP = ip;
        tvIpAddress.setText(ip);

        // 开启服务，并在服务中开启http服务器
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mServicIentent = new Intent(getApplicationContext(), WebService.class);
                //开启一个service
                startService(mServicIentent);


            }
        });

    }


    @Override
    protected void onDestroy() {
        if (mServicIentent!=null){
            // 关闭一个Service
            stopService(mServicIentent);
        }

        super.onDestroy();


    }


}
