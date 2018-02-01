package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.util.ApkUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 邀请安装
 * <p>
 * 蓝牙
 * wlan
 */
public class InviteActivity extends AppCompatActivity {


    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    private Context mContext;
    private Unbinder mUnbind;
    private static final String TAG = InviteActivity.class.getSimpleName();


    public static void start(Context context) {
        context.startActivity(new Intent(context, InviteActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        mContext = this;
        mUnbind = ButterKnife.bind(this);
        initUI();


    }

    /**
     * 通过蓝牙的方式分享
     *
     * @param view
     */
    @OnClick(R.id.btn_bluetooth)
    public void clickBluetoothInvite(View view) {

        String apkFilePath = ApkUtils.getApkFilePath(this, getPackageName());
        if (apkFilePath == null) {
            Toast.makeText(mContext, "获取应用安装包失败!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 通过蓝牙的方式发送本应的apk给对端
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*"); //
        intent.setClassName("com.android.bluetooth"
                , "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkFilePath)));
        startActivity(intent);

    }

    /**
     * 初始化UI
     */
    private void initUI() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("邀请安装");
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

        mUnbind.unbind();
        super.onDestroy();
    }
}
