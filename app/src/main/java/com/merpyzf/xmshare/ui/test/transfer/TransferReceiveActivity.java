package com.merpyzf.xmshare.ui.test.transfer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.adapter.FileTransferAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TransferReceiveActivity extends AppCompatActivity {

    private Unbinder mUnbinder;
    private Context mContext;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ReceiverManager mReceiver;
    private ExecutorService mThreadPool;


    public static void start(Context context) {

        context.startActivity(new Intent(context, TransferReceiveActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive2);

        mThreadPool = Executors.newSingleThreadExecutor();

        mUnbinder = ButterKnife.bind(this);
        mContext = this;

        initUI();


        mReceiver = ReceiverManager.getInstance();
        // 将线程的执行托管到线程池中进行, 开启一个ServerSocket,进行文件的接收
        mThreadPool.execute(mReceiver);

        mReceiver.setOnTransferFileListListener(receiveFileList -> {

            FileTransferAdapter fileTransferAdapter = new FileTransferAdapter<>(R.layout.item_rv_transfer, receiveFileList);
            mRecyclerView.setAdapter(fileTransferAdapter);

        });
    }


    /**
     * 初始化UI
     */
    private void initUI() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        mReceiver.release();


    }
}
