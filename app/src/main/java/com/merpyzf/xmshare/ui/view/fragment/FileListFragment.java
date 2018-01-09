package com.merpyzf.xmshare.ui.view.fragment;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.ui.adapter.FileAdapter;
import com.merpyzf.xmshare.ui.entity.MusicFile;
import com.merpyzf.xmshare.ui.entity.PicFile;
import com.merpyzf.xmshare.util.ApkUtils;
import com.merpyzf.xmshare.util.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
public class FileListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.rv_music_list)
    RecyclerView mRvFileList;
    @BindView(R.id.pb_music_waiting)
    ProgressBar mProgressBar;

    private Context mContext;
    private Unbinder mUnbinder;
    private LoaderManager mLoaderManager;
    // 要加载的文件类型
    private static int LOAD_FILE_TYPE = 1;

    private List<FileInfo> mFileLists;
    private FileAdapter mFileListAdapter;

    private File mParentAlbumFile;
    private Uri mUri;
    private String[] mProjections;
    private Handler mHandler;


    public FileListFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            LOAD_FILE_TYPE = bundle.getInt("load_file_type");
        }

        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mContext = getContext();

        initUI();
        init();

        return view;
    }


    private void initUI() {

        mFileLists = new ArrayList<>();

        switch (LOAD_FILE_TYPE) {

            case FileInfo.FILE_TYPE_APP:

                mRvFileList.setLayoutManager(new GridLayoutManager(getContext(), 4));

                break;

            case FileInfo.FILE_TYPE_IMAGE:

                mRvFileList.setLayoutManager(new GridLayoutManager(getContext(), 4));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_pic, mFileLists);

                break;

            case FileInfo.FILE_TYPE_MUSIC:
                mRvFileList.setLayoutManager(new LinearLayoutManager(getContext()));
                mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_music, mFileLists);
                break;

            case FileInfo.FILE_TYPE_VIDEO:
                mRvFileList.setLayoutManager(new LinearLayoutManager(getContext()));
                break;

            default:
                break;


        }

        // 设置适配器
        mRvFileList.setAdapter(mFileListAdapter);


    }

    private void init() {


        mLoaderManager = getActivity().getLoaderManager();
        mLoaderManager.initLoader(LOAD_FILE_TYPE, null, FileListFragment.this);

        if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_APP) {

            mHandler = new ApkHandler();

            // 异步扫描本地中已安装的应用
            new Thread(new Runnable() {
                @Override
                public void run() {

                    mFileLists = ApkUtils.getApp(getActivity(), getActivity().getPackageManager());
                    // 发送一个空的消息，提示扫描完毕

                    Log.i("wk","扫描完毕发送通知");

                    mHandler.sendEmptyMessage(0);

                }
            }).start();
        }


    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        Log.i("chk", "onCreateLoader->" + Thread.currentThread().getName());

        // 从本地数据库中扫描音乐文件
        if (id == FileInfo.FILE_TYPE_MUSIC) {

            System.out.println("onCreateLoader");

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
                            MediaStore.Images.Media.DATE_ADDED // 文件添加/修改时间
                    };

            return new CursorLoader(getContext(), mUri, mProjections, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        }


        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {

        if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_MUSIC) {
            // 遍历扫描到的音乐文件
            while (data.moveToNext()) {

                String title = data.getString(0);
                String artist = data.getString(1);
                String path = data.getString(2);
                int album_id = data.getInt(3);
                String mimeType = data.getString(4);
                long extra_max_bytes = data.getLong(5);
                long duration = data.getLong(6);
                Log.i("wk", "title:" + title + "\nartist:" + artist + "\npath:" + path + "\nalbum:" + album_id + "\nduration:" + duration + "\nmimieType:" + mimeType + "\n max_bytes:" + extra_max_bytes);
                MusicFile fileInfo = new MusicFile(title, path, "mp3", extra_max_bytes, album_id, artist, duration);

                mFileLists.add(fileInfo);

            }

            if (mFileLists.size() != 0) {
                if (mFileLists.size() == 0) {
                    Toast.makeText(getContext(), "没有扫描到本地音乐", Toast.LENGTH_SHORT).show();
                }
                // 数据加载完成后隐藏进进度提示
                mProgressBar.setVisibility(View.INVISIBLE);
                // 刷新数据
                mFileListAdapter.notifyDataSetChanged();
                // 在线程中更新音乐封面图片
                updateAlbumImg();
            }


        } else if (LOAD_FILE_TYPE == FileInfo.FILE_TYPE_IMAGE) {

            // 遍历扫描到的图片文件
            while (data.moveToNext()) {

                String id = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String path = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                File file = new File(path);

                //对图片的大小进行筛选
                if (file.length() > 2000) {

                    PicFile picFile = new PicFile();
                    picFile.setId(id);
                    picFile.setPath(path);

                    mFileLists.add(picFile);
                }

            }


            if (mFileLists.size() != 0) {
                if (mFileLists.size() == 0) {
                    Toast.makeText(getContext(), "没有扫描到本地图片", Toast.LENGTH_SHORT).show();
                }
                // 数据加载完成后隐藏进进度提示
                mProgressBar.setVisibility(View.INVISIBLE);
                // 刷新数据
                mFileListAdapter.notifyDataSetChanged();

            }
        }


    }

    /**
     * 更新封面图片
     */
    public void updateAlbumImg() {


        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mParentAlbumFile = new File(Environment.getExternalStorageDirectory(), Constant.ALBUM_IMG_PATH);
            if (!mParentAlbumFile.exists()) {
                mParentAlbumFile.mkdirs();
            }
        }
        MusicUtils.writeAlbumImg2local(getContext(), mParentAlbumFile, mFileLists);

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

            Log.i("wk", "扫描到的应用的长度:" + mFileLists.size());
            mFileListAdapter = new FileAdapter<FileInfo>(getActivity(), R.layout.item_rv_apk, mFileLists);
            mRvFileList.setAdapter(mFileListAdapter);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onDestroyView() {
        mLoaderManager.destroyLoader(LOAD_FILE_TYPE);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoaderManager.destroyLoader(LOAD_FILE_TYPE);
        mUnbinder.unbind();
    }

}
