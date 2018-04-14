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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.merpyzf.filemanager.widget.bean.Label;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.util.FileUtils;
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
public class PhotoDirsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, BaseQuickAdapter.OnItemClickListener {


    @BindView(R.id.rv_dirs_list)
    RecyclerView mRvDirsList;
    private LoaderManager mLoadManager;
    private Unbinder mUnbinder;
    private Context mContext;
    private List<PhotoDirBean> mPhotoDirs = new ArrayList<>();
    private ImgDirsAdapter mAdapter;
    private PhotoFragment mImageFragment;
    private int mPhotosNum = 0;


    public PhotoDirsFragment() {


        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_img_dirs, container, false);
        mContext = getContext();
        mUnbinder = ButterKnife.bind(this, rootView);


        initData();
        initUI();
        initEvent();

        // 只需要在此Fragment创建的时候加载一次数据，在它上面的frgment在退栈的时候会导致当前的fragment重新创建一次View
        if (mPhotoDirs.size() == 0) {
            mLoadManager = getActivity().getLoaderManager();
            mLoadManager.initLoader(FileInfo.FILE_TYPE_IMAGE, null, this);
        }


        return rootView;
    }

    private void initEvent() {

        mAdapter.setOnItemClickListener(this);

    }

    private void initUI() {


        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {

            if (fragment instanceof PhotoFragment) {

                mImageFragment = (PhotoFragment) fragment;


            }

        }

        mImageFragment.getTvTitle().setText("图片(" + mPhotosNum + ")");


        mRvDirsList.setLayoutManager(new LinearLayoutManager(mContext));
        // TODO: 2018/4/2 分割线需要美化
        mRvDirsList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mAdapter = new ImgDirsAdapter(R.layout.item_rv_photo_dir, mPhotoDirs);
        mRvDirsList.setAdapter(mAdapter);


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


        Log.i("wk", "执行了");


        Observable.just(data)
                .filter(cursor -> !cursor.isClosed())
                .map(cursor -> {

                    Log.i("w2k", "被观察者->" + Thread.currentThread().getName());

                    List<PhotoDirBean> mDirList = new ArrayList<>();

                    Set<String> dirSet;
                    if (cursor.getCount() > 0) {
                        dirSet = new HashSet<>();

                        while (cursor.moveToNext()) {

                            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));

                            File file = new File(path);
                            if (!dirSet.contains(file.getParent())) {


                                File[] images = file.getParentFile().listFiles(filterFile -> {


                                    String fileSuffix = FileUtils.getFileSuffix(filterFile.getPath()).toLowerCase();


                                    if (!fileSuffix.equals("") && filterFile.length() > 20 * 1024) {

                                        if (fileSuffix.equals("jpg") || fileSuffix.equals("jpeg")
                                                || fileSuffix.equals("gif") || fileSuffix.equals("png")
                                                || fileSuffix.equals("bpm") || fileSuffix.equals("webp")) {

                                            mPhotosNum++;

                                            return true;
                                        } else {
                                            return false;
                                        }


                                    } else {

                                        return false;
                                    }
                                });


                                if (images.length > 0) {

                                    PhotoDirBean photoDirBean = new PhotoDirBean();
                                    photoDirBean.setCoverImg(images[images.length - 1].getPath());
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

                    mPhotoDirs.clear();
                    mPhotoDirs.addAll(photoDirBeans);
                    mAdapter.notifyDataSetChanged();
                    mImageFragment.getTvTitle().setText("图片(" + mPhotosNum + ")");


                });


    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack("xm");

        PhotoDirBean item = (PhotoDirBean) adapter.getItem(position);

        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", item);
        fragmentTransaction.replace(R.id.fl_container, ShowPhotosFragment.newInstance(bundle));
        fragmentTransaction.commit();

        mImageFragment.getFileSelectIndicator().add(new Label(item.getName(), ""));

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
