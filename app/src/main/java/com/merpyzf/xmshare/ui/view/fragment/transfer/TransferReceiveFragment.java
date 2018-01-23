package com.merpyzf.xmshare.ui.view.fragment.transfer;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.adapter.FileTransferAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransferReceiveFragment extends Fragment {

    private Unbinder mUnbinder;
    private Context mContext;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ReceiverManager mReceiver;
    private ExecutorService mThreadPool;

    public TransferReceiveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_transfer_receive, container, false);

        mThreadPool = Executors.newSingleThreadExecutor();
        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();

        initUI();


        mReceiver = ReceiverManager.getInstance();
        // 将线程的执行托管到线程池中进行, 开启一个ServerSocket,进行文件的接收
        mThreadPool.execute(mReceiver);

        mReceiver.setOnTransferFileListListener(receiveFileList -> {

            FileTransferAdapter fileTransferAdapter = new FileTransferAdapter<>(R.layout.item_rv_transfer, FileTransferAdapter.TYPE_RECEIVE, receiveFileList);
            mRecyclerView.setAdapter(fileTransferAdapter);

        });



        return rootView;
    }

    /**
     * 初始化UI
     */
    private void initUI() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }


    @Override
    public void onDestroy() {
        mReceiver.release();
        super.onDestroy();
    }
}
