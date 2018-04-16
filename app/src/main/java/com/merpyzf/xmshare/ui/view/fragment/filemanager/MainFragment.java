package com.merpyzf.xmshare.ui.view.fragment.filemanager;


import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.BaseFragment;
import com.merpyzf.xmshare.util.FileUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends BaseFragment {

    private List<FileInfo> mDocFileList = new ArrayList<>();
    private List<FileInfo> mApkFileList = new ArrayList<>();
    private List<FileInfo> mCompactFileList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    /**
     * 加载数据
     */
    @Override
    protected void loadData() {
        super.loadData();

        Log.i("fm", "----> loadData方法执行了");

        String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
        Log.i("wk", "内置SD卡路径 -->" + externalStoragePath);
        String rootPath = Environment.getDataDirectory().getPath();
        Log.i("wk", "手机内存根路径 -->" + rootPath);
        String storagePath = getStoragePath(getContext());
        Log.i("wk", "手机扩展内存卡路径 -->" + storagePath);

        Observable.just("")
                // 设置观察者所在的线程
                .observeOn(Schedulers.io())
                .subscribe(s -> {


                    List<FileInfo> scanResults = FileUtils.traverseFolder(externalStoragePath);

                    for (FileInfo fileInfo : scanResults) {

                        if (fileInfo.getType() == FileInfo.FILE_TYPE_DOCUMENT) {
                            mDocFileList.add(fileInfo);
                        } else if (fileInfo.getType() == FileInfo.FILE_TYPE_APP) {
                            mApkFileList.add(fileInfo);
                        } else if (fileInfo.getType() == FileInfo.FILE_TYPE_COMPACT) {
                            mCompactFileList.add(fileInfo);
                        }

                        // 两者都进行更新
                    }


                    Log.i("wk", "文档数量-->" + mDocFileList.size() + "apk " + mApkFileList.size() + "zip: " + mCompactFileList.size());

                    // TODO: 2018/4/16 1. 后台计算集合中文件的md5值 2.缓存apk的图标


                });


    }

    /**
     * 对View进行相关的初始化工作
     *
     * @param mRootView
     */
    @Override
    protected void initView(View mRootView) {

        Log.i("fm", "----> initView方法执行了");

    }


    /**
     * 获取扩展内存的路径
     *
     * @param mContext
     * @return
     */
    public String getStoragePath(Context mContext) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
