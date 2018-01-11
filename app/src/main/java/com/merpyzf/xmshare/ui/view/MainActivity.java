package com.merpyzf.xmshare.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.merpyzf.httpcoreserver.ui.HttpServerActivity;
import com.merpyzf.transfermanager.receive.Receiver;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.util.SharedPreUtils;

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
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.edt_nickname)
    EditText edtNickName;


    private String TAG = MainActivity.class.getSimpleName();
    private Context mContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mbind = ButterKnife.bind(this);

        init();

        initEvent();


    }

    /**
     * 初始化参数
     */
    private void init() {

        setNickName();

    }


    private void initEvent() {

        btnStartServer.setOnClickListener(this);
        btnReceive.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnStartSs.setOnClickListener(this);
        btnSave.setOnClickListener(this);


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
            case R.id.btn_save:

                // TODO: 2018/1/11 在应用开启并且没有手动设置设备昵称时，获取设备的设备名作为设备的昵称
                String nickName = edtNickName.getText().toString().trim();

                if (!nickName.equals("")) {
                    SharedPreUtils.putString(mContext, Constant.SP_USER, "nickName", nickName);
                } else {
                    Toast.makeText(mContext, "昵称不能为空！", Toast.LENGTH_SHORT).show();
                }

                break;

            default:
                break;
        }

    }

    /**
     * 设置昵称
     */
    private void setNickName() {

        String nickName = SharedPreUtils.getString(mContext, Constant.SP_USER, "nickName", "");
        if (!nickName.equals("")) {
            edtNickName.setText(nickName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}

