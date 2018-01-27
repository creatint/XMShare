package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Switch;

import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.util.SharedPreUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 设置页面
 */
public class SettingActivity extends AppCompatActivity {
    private static final String TAG = SendActivity.class.getSimpleName();
    private Unbinder mUnbinder;
    private Context mContext;
    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    // 设置建立局域网的方式
    @BindView(R.id.switch_transfer_mode)
    Switch mSwitchTransferMode;


    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        mUnbinder = ButterKnife.bind(this);

        initUI();
        initEvent();

    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        mSwitchTransferMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                // 热点
                SharedPreUtils.putInteger(mContext, Constant.SP_USER, Constant.KEY_TRANSFER_MODE,
                        Constant.TRANSFER_MODE_AP);
                Log.i(TAG, "设置 AP模式");

            } else {
                // 局域网
                SharedPreUtils.putInteger(mContext, Constant.SP_USER, Constant.KEY_TRANSFER_MODE,
                        Constant.TRANSFER_MODE_LAN);
                Log.i(TAG, "设置局域网模式");
            }
        });


    }

    /**
     * 初始化界面
     */
    private void initUI() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int transferMode = SharedPreUtils.getInteger(mContext, Constant.SP_USER, Constant.KEY_TRANSFER_MODE, Constant.TRANSFER_MODE_LAN);

        if (transferMode == Constant.TRANSFER_MODE_LAN) {
            mSwitchTransferMode.setChecked(false);
        } else if (transferMode == Constant.TRANSFER_MODE_AP) {
            mSwitchTransferMode.setChecked(true);
        }


    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }
}
