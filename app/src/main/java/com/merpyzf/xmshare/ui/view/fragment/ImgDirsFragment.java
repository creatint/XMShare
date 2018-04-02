package com.merpyzf.xmshare.ui.view.fragment;


import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.bean.model.PhotoDirBean;
import com.merpyzf.xmshare.ui.adapter.ImgDirsAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 设备中相册列表的展示
 */
public class ImgDirsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    @BindView(R.id.rv_dirs_list)
    RecyclerView mRvDirsList;
    private LoaderManager mLoadManager;
    private Unbinder mUnbinder;
    private Context mContext;


    public ImgDirsFragment() {


        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_img_dirs, container, false);
        mContext = getContext();
        mUnbinder = ButterKnife.bind(this, rootView);


        initUI();
        initData();


        mLoadManager = getActivity().getLoaderManager();
        mLoadManager.initLoader(FileInfo.FILE_TYPE_IMAGE, null, this);


        return rootView;
    }

    private void initUI() {

        mRvDirsList.setLayoutManager(new LinearLayoutManager(mContext));

        // TODO: 2018/4/2 分割线需要美化
        mRvDirsList.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));



    }

    private void initData() {



    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        if (id == FileInfo.FILE_TYPE_IMAGE) {

            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projections = new String[]
                    {
                            MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DATA, // 文件路径
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Images.Media.DATE_ADDED // 文件添加/修改时间
                    };

            return new CursorLoader(getContext(), uri, projections, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {


        Observable.just(data)
                .map(cursor -> {

                    Log.i("w2k", "被观察者->"+Thread.currentThread().getName());

                    List<PhotoDirBean> mDirList = new ArrayList<>();

                    Set<String> dirSet;
                    if (data.getCount() > 0) {
                        dirSet = new HashSet<>();

                        while (data.moveToNext()) {

                            String id = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            String path = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                            String title = data.getString(data.getColumnIndex(MediaStore.Video.Media.TITLE));

                            File file = new File(path);
                            if (!dirSet.contains(file.getParent())) {


                                File[] images = file.getParentFile().listFiles(filterFile -> {

                                    if (filterFile.length() > 2000) {

                                        return true;
                                    }

                                    return false;
                                });


                                if (images.length > 0) {

                                    PhotoDirBean photoDirBean = new PhotoDirBean();
                                    photoDirBean.setCoverImg(images[0].getPath());
                                    photoDirBean.setName(file.getParentFile().getName());
                                    photoDirBean.setImageList(images);

                                    mDirList.add(photoDirBean);

                                }

                                dirSet.add(file.getParent());
                            }

                        }

                    }
                    return mDirList;
                })
                // 设置被观察者所在的线程
                .subscribeOn(Schedulers.io())
                // 设置观察者所在的线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(photoDirBeans -> {


                    ImgDirsAdapter imgDirsAdapter = new ImgDirsAdapter(R.layout.item_photo_dir, photoDirBeans);
                    mRvDirsList.setAdapter(imgDirsAdapter);

                });




    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


    @Override
    public void onDestroyView() {

        mLoadManager.destroyLoader(FileInfo.FILE_TYPE_IMAGE);
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        mLoadManager.destroyLoader(FileInfo.FILE_TYPE_IMAGE);
        super.onDestroy();
    }
}
