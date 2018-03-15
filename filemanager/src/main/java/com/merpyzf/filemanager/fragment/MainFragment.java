package com.merpyzf.filemanager.fragment;


import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.merpyzf.filemanager.R;
import com.merpyzf.filemanager.base.BaseFragment;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends BaseFragment {


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
        Log.i("wk","内置SD卡路径 -->"+externalStoragePath);
        String rootPath = Environment.getDataDirectory().getPath();
        Log.i("wk", "手机内存根路径 -->"+rootPath);
        String storagePath = getStoragePath(getContext());
        Log.i("wk", "手机扩展内存卡路径 -->"+storagePath);


    }

    /**
     * 对View进行相关的初始化工作
     * @param mRootView
     */
    @Override
    protected void initView(View mRootView) {

        Log.i("fm","----> initView方法执行了");


    }



    /**
     * 获取扩展内存的路径
     *
     * @param mContext
     * @return
     */
    public  String getStoragePath(Context mContext) {

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
