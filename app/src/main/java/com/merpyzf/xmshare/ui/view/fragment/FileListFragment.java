package com.merpyzf.xmshare.ui.view.fragment;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.transfermanager.util.FileUtils;
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.receiver.FileSelectedListChangedReceiver;
import com.merpyzf.xmshare.ui.adapter.FileAdapter;
import com.merpyzf.xmshare.ui.view.activity.OnFileSelectListener;
import com.merpyzf.xmshare.ui.view.activity.SelectFilesActivity;
import com.merpyzf.xmshare.util.AnimationUtils;
import com.merpyzf.xmshare.util.ApkUtils;
import com.merpyzf.xmshare.util.Md5Utils;
import com.merpyzf.xmshare.util.MusicUtils;
import com.merpyzf.xmshare.util.VideoUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.merpyzf.transfermanager.entity.FileInfo.FILE_TYPE_APP;
import static com.merpyzf.transfermanager.entity.FileInfo.FILE_TYPE_IMAGE;
import static com.merpyzf.transfermanager.entity.FileInfo.FILE_TYPE_MUSIC;
import static com.merpyzf.transfermanager.entity.FileInfo.FILE_TYPE_VIDEO;

/**
 * 扫描到的本地文件列表的展示页面
 *
 * @author wangke
 */
public class FileListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.rv_music_list)
    FastScrollRecyclerView mRvFileList;
    @BindView(R.id.pb_music_waiting)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    // 全选
    @BindView(R.id.checkbox_all)
    CheckBox mCheckBoxAll;
    @BindView(R.id.tv_checked)
    TextView mTvChecked;

    private Context mContext;
    private Unbinder mUnbinder;
    private LoaderManager mLoaderManager;
    // 要加载的文件类型
    private int LOAD_FILE_TYPE = 1;

    private List<FileInfo> mFileLists;
    private FileAdapter mFileListAdapter;

    private Handler mHandler;
    private OnFileSelectListener<FileInfo> mFileSelectListener;
    private FileSelectedListChangedReceiver mFslcReceiver;
    private View bottomSheetView;
    private static final String TAG = FileListFragment.class.getSimpleName();

    @SuppressLint("ValidFragment")
    public FileListFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    private FileListFragment(int type, OnFileSelectListener<FileInfo> fileSelectListener) {
        super();
        this.LOAD_FILE_TYPE = type;
        this.mFileSelectListener = fileSelectListener;
    }

    public static FileListFragment newInstance(int type, OnFileSelectListener<FileInfo> fileSelectListener) {

        return new FileListFragment(type, fileSelectListener);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getContext();
        initUI();

        if (LOAD_FILE_TYPE == FILE_TYPE_APP) {
            asyncLoadApp();
        }

        mLoaderManager = getActivity().getLoaderManager();
        mLoaderManager.initLoader(LOAD_FILE_TYPE, null, FileListFragment.this);

        // 选择前先清空上一次选择的数据
        if (App.getSendFileList().size() > 0) {
            App.getSendFileList().clear();
        }


        mFileListAdapter.setOnItemClickListener((adapter, view1, position) -> {

            ImageView ivSelect = view1.findViewById(R.id.iv_select);

            File path = App.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            Log.i("wk", path.getAbsolutePath());


            FileInfo fileInfo = mFileLists.get(position);

            if (!App.getSendFileList().contains(fileInfo)) {

                ivSelect.setVisibility(View.VISIBLE);
                // 添加选中的文件
                App.addSendFile(fileInfo);
                fileInfo.setMd5(Md5Utils.getFileMd5(fileInfo));
                // 将文件选择的事件回调给外部
                if (mFileSelectListener != null) {
                    mFileSelectListener.onSelected(fileInfo);

                }
                //2.添加任务 动画
                View startView = null;
                View targetView = null;

                startView = view1.findViewById(R.id.iv_cover);
                if (getActivity() != null && (getActivity() instanceof SelectFilesActivity)) {
                    SelectFilesActivity chooseFileActivity = (SelectFilesActivity) getActivity();
                    targetView = bottomSheetView;
                }

                AnimationUtils.setAddTaskAnimation(getActivity(), startView, targetView, null);


            } else {
                ivSelect.setVisibility(View.INVISIBLE);
                //移除选中的文件
                App.removeSendFile(fileInfo);
                if (mFileSelectListener != null) {
                    mFileSelectListener.onCancelSelected(fileInfo);
                }

            }


        });

        // 当选择的文件列表发生改变时的回调
        mFslcReceiver = new FileSelectedListChangedReceiver() {
            @Override
            public void onFileListChanged() {
                // 当选择的文件列表发生改变时的回调
                mFileListAdapter.notifyDataSetChanged();
            }
        };


        mCheckBoxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 全选
            if (isChecked) {

                mTvChecked.setText("取消全选");
                if (mFileSelectListener != null) {
                    mFileSelectListener.onCheckedAll(mFileLists);
                }

                mFileListAdapter.notifyDataSetChanged();
                mTvChecked.setText("取消全选");


            } else {
                if (mFileSelectListener != null) {
                    mFileSelectListener.onCancelCheckedAll(mFileLists);
                }
                mFileListAdapter.notifyDataSetChanged();
                mTvChecked.setText("全选");

            }

        });


        return rootView;
    }


    /**
     * 初始化UI
     */
    private void initUI() {

        bottomSheetView = getActivity().findViewById(R.id.bottom_sheet);

        if (mCheckBoxAll.isChecked()) {
            mTvChecked.setText("取消全选");
        } else {
            mTvChecked.setText("全选");
        }


        mFileLists = new ArrayList<>();

        switch (LOAD_FILE_TYPE) {

            case FILE_TYPE_APP:

                setTitle("应用");
                mRvFileList.setLayoutManager(new GridLayoutManager(getContext(), 4));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_apk, mFileLists);

                break;

            case FILE_TYPE_MUSIC:

                setTitle("音乐");
                mRvFileList.setLayoutManager(new LinearLayoutManager(getContext()));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_music, mFileLists);
                break;

            case FILE_TYPE_VIDEO:
                setTitle("视频");
                mRvFileList.setLayoutManager(new LinearLayoutManager(getContext()));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_video, mFileLists);
                break;

            default:
                break;


        }
        // 设置空布局
        // 需要将布局文件转换成View后才能设置，否则会报错
        mFileListAdapter.setEmptyView(View.inflate(mContext, R.layout.view_rv_file_empty, null));
        // 设置适配器
        mRvFileList.setAdapter(mFileListAdapter);
    }

    /**
     * 异步加载本地的app应用
     */
    private void asyncLoadApp() {
        mHandler = new ApkHandler();
        // 异步扫描本地中已安装的应用
        new Thread(() -> {
            // 直接将获取到的app集合赋值给mFileLists会改变指针的指向，从而对适配器使用notifyDataSetChanged()失效
            List<FileInfo> appList = ApkUtils.getApp(getActivity(), getActivity().getPackageManager());
            mFileLists.addAll(appList);
            // 将apk的ico写入到缓存文件中
            ApkUtils.asyncCacheApkIco(mContext, mFileLists);
            // 发送一个空的消息，提示扫描完毕
            mHandler.sendEmptyMessage(0);
            // 异步生成并文件的MD5并写入到数据库中
            Md5Utils.asyncGenerateFileMd5(mFileLists);

        }).start();
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // 这个方法执行在主线程

        Uri uri;
        String[] projections;
        // 扫描音乐文件
        if (id == FILE_TYPE_MUSIC) {

            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            projections = new String[]{
                    MediaStore.Audio.Media.TITLE,  //音乐名
                    MediaStore.Audio.Media.ARTIST, // 艺术家
                    MediaStore.Audio.Media.DATA, //音乐文件所在路径
                    MediaStore.Audio.Media.ALBUM_ID, // 音乐封面
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DURATION //音乐时长
            };

            return new CursorLoader(getContext(), uri, projections, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

            // 扫描图片文件
        } else if (id == FILE_TYPE_IMAGE) {

            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projections = new String[]
                    {
                            MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DATA, // 文件路径
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Images.Media.DATE_ADDED // 文件添加/修改时间
                    };

            return new CursorLoader(getContext(), uri, projections, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

            // 扫描视频文件
        } else if (id == FILE_TYPE_VIDEO) {

            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            projections = new String[]
                    {
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media.DATA, // 文件路径
                            MediaStore.Video.Media.DATE_ADDED // 文件添加/修改时间
                    };

            return new CursorLoader(getContext(), uri, projections, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");

        }


        return null;
    }


    @Override
    public void onLoadFinished(Loader loader, Cursor data) {


        if (LOAD_FILE_TYPE == FILE_TYPE_MUSIC) {

            Observable.just(data)
                    .flatMap(cursor -> {
                        // 遍历扫描到的音乐文件
                        while (data.moveToNext()) {

                            String title = data.getString(0);
                            String artist = data.getString(1);
                            String path = data.getString(2);
                            int album_id = data.getInt(3);
                            Log.i("album_id", "name-->" + title + "album_id-->" + album_id);
                            // 注意这边的音乐文件的大小是否正确
                            long extra_max_bytes = data.getLong(5);
                            long duration = data.getLong(6);
                            MusicFile fileInfo = new MusicFile(title, path, FILE_TYPE_MUSIC, (int) extra_max_bytes, album_id, artist, duration);
                            // 添加文件的后缀名
                            fileInfo.setSuffix(FileUtils.getFileSuffix(path));
                            if (extra_max_bytes > 1024 * 1024) {
                                mFileLists.add(fileInfo);
                            }

                        }
                        return Observable.just(mFileLists);
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(fileInfoList -> {


                        if (fileInfoList.size() == 0) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getContext(), "没有扫描到本地音乐", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 刷新界面
                        setTitle("音乐");
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mRvFileList.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mFileListAdapter.notifyDataSetChanged();

                        // 在线程中更新音乐封面图片
                        MusicUtils.updateAlbumImg(getContext(), mFileLists);
                        // 异步生成并文件的MD5并写入到数据库中
                        Md5Utils.asyncGenerateFileMd5(mFileLists);

                    });


        } else if (LOAD_FILE_TYPE == FILE_TYPE_VIDEO) {


            Observable.just(data)
                    .flatMap(cursor -> {

                        while (data.moveToNext()) {

                            String id = data.getString(data.getColumnIndex(MediaStore.Video.Media._ID));
                            String title = data.getString(data.getColumnIndex(MediaStore.Video.Media.TITLE));
                            long duration = data.getLong(data.getColumnIndex(MediaStore.Video.Media.DURATION));
                            String path = data.getString(data.getColumnIndex(MediaStore.Video.Media.DATA));
                            long length = new File(path).length();

                            VideoFile videoFile = new VideoFile();
                            videoFile.setAlbumId(id);
                            videoFile.setName(title);
                            videoFile.setPath(path);
                            videoFile.setLength((int) length);
                            videoFile.setDuration(duration);
                            // 设置文件后缀
                            videoFile.setSuffix(FileUtils.getFileSuffix(path));
                            // 设置文件类型
                            videoFile.setType(FILE_TYPE_VIDEO);

                            // 筛选大于1MB的文件
                            if (length > 1024 * 1024) {
                                mFileLists.add(videoFile);
                            }
                        }
                        return Observable.just(mFileLists);
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(fileInfoList -> {

                        if (fileInfoList.size() == 0) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(mContext, "没有在当前设备上扫描到视频", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 更新UI
                        setTitle("视频");
                        mRvFileList.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mFileListAdapter.notifyDataSetChanged();

                        VideoUtils.updateThumbImg(mContext, mFileLists);
                        Md5Utils.asyncGenerateFileMd5(mFileLists);

                    });


        }

    }


    @Override
    public void onLoaderReset(Loader loader) {


    }

    /**
     * 本地应用扫描完毕后的Handler通知处理
     */
    class ApkHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setTitle("应用");
            mFileListAdapter.notifyDataSetChanged();
            // 将RecyclerView设置为可见状态
            mRvFileList.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }


    public void setTitle(String type) {
        mTvTitle.setText(type + "(" + mFileLists.size() + ")");
    }


    @Override
    public void onDestroyView() {

        mLoaderManager.destroyLoader(LOAD_FILE_TYPE);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 解注册
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mFslcReceiver);

    }

    @Override
    public void onDestroy() {
        mLoaderManager.destroyLoader(LOAD_FILE_TYPE);
        mUnbinder.unbind();
        super.onDestroy();
    }

}
