package com.merpyzf.xmshare.ui.view.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Switch;

import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.common.base.BaseActivity;
import com.merpyzf.xmshare.util.SharedPreUtils;

import butterknife.BindView;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity {


    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    // 设置建立局域网的方式
    @BindView(R.id.switch_transfer_mode)
    Switch mSwitchTransferMode;
    private static final String TAG = SendActivity.class.getSimpleName();


    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

        int transferMode = SharedPreUtils.getInteger(mContext, Constant.SP_USER, Constant.KEY_TRANSFER_MODE, Constant.TRANSFER_MODE_LAN);

        if (transferMode == Constant.TRANSFER_MODE_LAN) {
            mSwitchTransferMode.setChecked(false);
        } else if (transferMode == Constant.TRANSFER_MODE_AP) {
            mSwitchTransferMode.setChecked(true);
        }

    }

    @Override
    public void initEvents() {

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

    @Override
    protected void initToolBar() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;

        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
