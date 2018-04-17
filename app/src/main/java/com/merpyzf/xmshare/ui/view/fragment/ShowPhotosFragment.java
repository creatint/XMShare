package com.merpyzf.xmshare.ui.view.fragment;


import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.bean.model.PhotoDirBean;
import com.merpyzf.xmshare.receiver.FileSelectedListChangedReceiver;
import com.merpyzf.xmshare.ui.adapter.FileAdapter;
import com.merpyzf.xmshare.ui.view.activity.SelectFilesActivity;
import com.merpyzf.xmshare.util.AnimationUtils;
import com.merpyzf.xmshare.util.Md5Utils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 展示相册中图片的Fragment
 */

public class ShowPhotosFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener, CompoundButton.OnCheckedChangeListener {


    private List<FileInfo> mImages = null;
    private View mRootView;
    private Unbinder mUnbind;
    private Context mContext;
    private View mBottomSheetView;
    private PhotoFragment mPhotoFrg;

    @BindView(R.id.rv_photo_list)
    RecyclerView mRvPhotoList;
    private FileAdapter<FileInfo> mAdapter;
    private FileSelectedListChangedReceiver mFslcReceiver;
    private CheckBox mCheckBoxAll;
    private static final String TAG = ShowPhotosFragment.class.getSimpleName();

    public static ShowPhotosFragment newInstance(Bundle args) {
        ShowPhotosFragment fragment = new ShowPhotosFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoDirBean photoDirBean = (PhotoDirBean) getArguments().getSerializable("photos");
        mImages = photoDirBean.getImageList();

    }

    @Override
    public void onResume() {
        super.onResume();

        // 动态注册监听选择文件列表发生改变的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileSelectedListChangedReceiver.ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mFslcReceiver, intentFilter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_test, container, false);
        }

        mContext = getContext();
        mUnbind = ButterKnife.bind(this, mRootView);
        initData();
        initUI();
        initEvent();


        return mRootView;
    }

    private void initData() {

        for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {

            if (fragment instanceof PhotoFragment) {
                mPhotoFrg = (PhotoFragment) fragment;
            }

        }


    }

    private void initEvent() {

        mAdapter.setOnItemClickListener(this);
        mCheckBoxAll.setOnCheckedChangeListener(this);

        // 当选择的文件列表发生改变时的回调
        mFslcReceiver = new FileSelectedListChangedReceiver() {
            @Override
            public void onFileListChanged() {

                Log.i("wk", "文件列表发生变化了");
                // 当选择的文件列表发生改变时的回调
                mAdapter.notifyDataSetChanged();
            }
        };


    }

    /**
     *
     */
    private void initUI() {


        mBottomSheetView = getActivity().findViewById(R.id.bottom_sheet);
        mRvPhotoList.setLayoutManager(new GridLayoutManager(mContext, 4));
        mAdapter = new FileAdapter<>(getActivity(), R.layout.item_rv_pic, mImages);
        mRvPhotoList.setAdapter(mAdapter);
        if (mPhotoFrg == null) return;
        mCheckBoxAll = mPhotoFrg.getCheckbox();
        mPhotoFrg.getTvTitle().setText("图片(" + mImages.size() + ")");
    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {


        ImageView ivSelect = view.findViewById(R.id.iv_select);

        File path = App.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Log.i("wk", path.getAbsolutePath());


        FileInfo fileInfo = mImages.get(position);

        if (!App.getSendFileList().contains(fileInfo)) {

            ivSelect.setVisibility(View.VISIBLE);
            // 添加选中的文件
            App.addSendFile(fileInfo);
            fileInfo.setMd5(Md5Utils.getFileMd5(fileInfo));

            if (mPhotoFrg != null) {
                mPhotoFrg.selectPhoto((PicFile) fileInfo);
            }

            View startView = null;
            View targetView = null;

            startView = view.findViewById(R.id.iv_cover);
            AnimationUtils.zoomOutCover(startView, 200);

            if (getActivity() != null && (getActivity() instanceof SelectFilesActivity)) {
                SelectFilesActivity chooseFileActivity = (SelectFilesActivity) getActivity();
                targetView = mBottomSheetView;
            }

            AnimationUtils.setAddTaskAnimation(getActivity(), startView, targetView, null);


        } else {


            AnimationUtils.zoomInCover(view.findViewById(R.id.iv_cover), 200);

            ivSelect.setVisibility(View.INVISIBLE);
            //移除选中的文件
            App.removeSendFile(fileInfo);

            // 将移除文件的事件通知外部
            if (mPhotoFrg != null) {
                mPhotoFrg.cancelSelectPhoto((PicFile) fileInfo);
            }

        }


    }


    @Override
    public void onPause() {
        super.onPause();
        // 解注册
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mFslcReceiver);

    }


    @Override
    public void onDestroy() {
        Log.i("wk", "onDestroy方法执行了");
        mContext = null;
        mFslcReceiver = null;
        mUnbind.unbind();
        super.onDestroy();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mPhotoFrg.checkAllPhoto(mImages);
        } else {
            mPhotoFrg.cancelCheckAllPhoto(mImages);
        }

        mAdapter.notifyDataSetChanged();
    }
}
