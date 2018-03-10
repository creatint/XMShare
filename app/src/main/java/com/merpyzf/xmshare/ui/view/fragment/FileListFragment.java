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
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.transfermanager.util.FileUtils;
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.bean.model.FileMd5Model;
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
import io.reactivex.schedulers.Schedulers;

/**
 *
 * 扫描到的本地文件列表的展示页面
 * @author wangke
 *
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

    private Uri mUri;
    private String[] mProjections;
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
        FileListFragment fileListFragment = new FileListFragment(type, fileSelectListener);

        return fileListFragment;
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

        View rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getContext();
        initUI();

        if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_APP) {
            asyncLoadApp();
        }

        mLoaderManager = getActivity().getLoaderManager();
        mLoaderManager.initLoader(LOAD_FILE_TYPE, null, FileListFragment.this);

        // 选择前先清空上一次选择的数据
        if (App.getSendFileList().size() > 0) {
            App.getSendFileList().clear();
        }

        /**
         *  文件列表点击选择的回调事件
         */
        mFileListAdapter.setOnItemClickListener((adapter, view1, position) -> {

            ImageView ivSelect = view1.findViewById(R.id.iv_select);


            FileInfo fileInfo = mFileLists.get(position);

            if (!App.getSendFileList().contains(fileInfo)) {

                ivSelect.setVisibility(View.VISIBLE);
                // 添加选中的文件
                App.addSendFile(fileInfo);


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
                for (int i = 0; i < mFileLists.size(); i++) {
                    if (!App.getSendFileList().contains(mFileLists.get(i))) {
                        App.getSendFileList().add(mFileLists.get(i));
                    }
                }
                mFileListAdapter.notifyDataSetChanged();
                if (mFileSelectListener != null) {
                    mFileSelectListener.onCheckedAll();
                }

                mTvChecked.setText("取消全选");


            } else {

                // 取消全选
                for (int i = 0; i < mFileLists.size(); i++) {
                    if (App.getSendFileList().contains(mFileLists.get(i))) {
                        App.getSendFileList().remove(mFileLists.get(i));
                    }
                }
                mFileListAdapter.notifyDataSetChanged();
                if (mFileSelectListener != null) {
                    mFileSelectListener.onCheckedAll();
                }

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

            case FileInfo.FILE_TYPE_APP:

                setTitle("应用");
                mRvFileList.setLayoutManager(new GridLayoutManager(getContext(), 4));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_apk, mFileLists);

                break;

            case FileInfo.FILE_TYPE_IMAGE:

                setTitle("图片");
                mRvFileList.setLayoutManager(new GridLayoutManager(getContext(), 4));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_pic, mFileLists);

                break;

            case FileInfo.FILE_TYPE_MUSIC:

                setTitle("音乐");
                mRvFileList.setLayoutManager(new LinearLayoutManager(getContext()));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_music, mFileLists);
                break;

            case FileInfo.FILE_TYPE_VIDEO:
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
        mRvFileList.setVisibility(View.INVISIBLE);


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
            // 发送一个空的消息，提示扫描完毕
            mHandler.sendEmptyMessage(0);
            // 异步生成并文件的MD5并写入到数据库中
            asyncGenerateFileMd5(mFileLists);

        }).start();
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        Log.i("chk", "onCreateLoader->" + Thread.currentThread().getName());

        // 从本地数据库中扫描音乐文件
        if (id == FileInfo.FILE_TYPE_MUSIC) {

            mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            mProjections = new String[]{
                    MediaStore.Audio.Media.TITLE,  //音乐名
                    MediaStore.Audio.Media.ARTIST, // 艺术家
                    MediaStore.Audio.Media.DATA, //音乐文件所在路径
                    MediaStore.Audio.Media.ALBUM_ID, // 音乐封面
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DURATION //音乐时长
            };

            return new CursorLoader(getContext(), mUri, mProjections, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

            // 从本地数据库中扫描本地图片
        } else if (id == FileInfo.FILE_TYPE_IMAGE) {

            mUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mProjections = new String[]
                    {
                            MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DATA, // 文件路径
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Images.Media.DATE_ADDED // 文件添加/修改时间
                    };

            return new CursorLoader(getContext(), mUri, mProjections, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        } else if (id == FileInfo.FILE_TYPE_VIDEO) {

            mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            mProjections = new String[]
                    {
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media.DATA, // 文件路径
                            MediaStore.Video.Media.DATE_ADDED // 文件添加/修改时间
                    };

            return new CursorLoader(getContext(), mUri, mProjections, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");

        }


        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {


        if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_MUSIC) {

            if (data.getCount() > 0) {


                // 遍历扫描到的音乐文件
                while (data.moveToNext()) {

                    String title = data.getString(0);
                    String artist = data.getString(1);
                    String path = data.getString(2);
                    int album_id = data.getInt(3);
                    String mimeType = data.getString(4);

                    // 注意这边的音乐文件的大小是否正确
                    long extra_max_bytes = data.getLong(5);
                    long duration = data.getLong(6);
                    MusicFile fileInfo = new MusicFile(title, path, FileInfo.FILE_TYPE_MUSIC, (int) extra_max_bytes, album_id, artist, duration);
                    // 添加文件的后缀名
                    fileInfo.setSuffix(FileUtils.getFileSuffix(path));

                    Log.i(TAG, "name->" + title + " extra_max_bytes->" + extra_max_bytes);

                    // 筛选大于1MB的文件
                    if (extra_max_bytes > 1024 * 1024) {

                        mFileLists.add(fileInfo);
                    }

                }

                setTitle("音乐");
                // 数据加载完成后隐藏进进度提示
                mProgressBar.setVisibility(View.INVISIBLE);
                // 在线程中更新音乐封面图片
                MusicUtils.updateAlbumImg(getContext(), mFileLists);
                // 异步生成并文件的MD5并写入到数据库中
                asyncGenerateFileMd5(mFileLists);

            } else {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "没有扫描到本地音乐", Toast.LENGTH_SHORT).show();
            }

            mRvFileList.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            mFileListAdapter.notifyDataSetChanged();


        } else if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_IMAGE) {

            if (data.getCount() > 0) {

                // 遍历扫描到的图片文件
                while (data.moveToNext()) {

                    String id = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String path = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String title = data.getString(data.getColumnIndex(MediaStore.Video.Media.TITLE));

                    File file = new File(path);

                    //对图片的大小进行筛选
                    if (file.length() > 2000) {

                        PicFile picFile = new PicFile();
                        picFile.setId(id);
                        picFile.setPath(path);
                        picFile.setName(title);
                        // 设置文件的大小
                        picFile.setLength((int) new File(path).length());
                        picFile.setSuffix(FileUtils.getFileSuffix(path));
                        // 设置文件的类型
                        picFile.setType(FileInfo.FILE_TYPE_IMAGE);
                        mFileLists.add(picFile);
                    }

                }
                setTitle("图片");
                // 数据加载完成后隐藏进进度提示
                mRvFileList.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                // 异步生成并文件的MD5并写入到数据库中
                asyncGenerateFileMd5(mFileLists);
            } else {
                // 数据加载完成后隐藏进进度提示
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(mContext, "没有在当前设备上扫描到图片", Toast.LENGTH_SHORT).show();


            }
            // 刷新数据
            mFileListAdapter.notifyDataSetChanged();


        } else if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_VIDEO) {

            if (data.getCount() > 0) {


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
                    videoFile.setType(FileInfo.FILE_TYPE_VIDEO);

                    // 筛选大于1MB的文件
                    if (length > 1024 * 1024) {
                        mFileLists.add(videoFile);
                    }

                }
                setTitle("视频");
                mRvFileList.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                VideoUtils.updateThumbImg(mContext, mFileLists);
                // 异步生成并文件的MD5并写入到数据库中
                asyncGenerateFileMd5(mFileLists);



            } else {

                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(mContext, "没有在当前设备上扫描到视频", Toast.LENGTH_SHORT).show();

            }

            mFileListAdapter.notifyDataSetChanged();


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


    /**
     * 生成文件的Md5值并存储到数据库中
     */
    public void asyncGenerateFileMd5(List<FileInfo> fileList){

        LiteOrm liteOrm = App.getSingleLiteOrm();

        Observable.fromIterable(fileList)
                .filter(fileInfo -> {
                    // 过滤掉数据库中已经存在的文件
                    ArrayList<FileMd5Model> fileMd5Models = liteOrm.query(new QueryBuilder<FileMd5Model>(FileMd5Model.class)
                            .whereEquals("file_name", fileInfo.getName()));
                    if(fileMd5Models.size() == 0){
                        Log.i(TAG,fileInfo.getName()+"在数据库中不存在");
                        return true;
                    }

                    Log.i(TAG,fileInfo.getName()+"在数据库中已经存在");
                    return false;
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(fileInfo -> {
                    // 计算文件的MD5耗时操作
                    String md5 = Md5Utils.getMd5(new File(fileInfo.getPath()));
                    Log.i(TAG, "计算"+fileInfo.getName()+"的MD5,并向数据库中写入");
                    FileMd5Model fileMd5Model = new FileMd5Model(fileInfo.getName(), md5);
                    // 向数据库中写入
                    liteOrm.insert(fileMd5Model);

                });






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
