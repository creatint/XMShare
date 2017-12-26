package com.merpyzf.xmshare.ui.view.fragment;


import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.ui.adapter.FileAdapter;
import com.merpyzf.xmshare.ui.entity.MusicFile;
import com.merpyzf.xmshare.util.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private LoaderManager mLoaderManager;
    private static final int LOAD_MUSIC = 1;
    private List<MusicFile> mMusicList;
    private Unbinder mUnbinder;
    private Context mContext;
    @BindView(R.id.rv_music_list)
    RecyclerView mRvMusicList;
    @BindView(R.id.pb_music_waiting)
    ProgressBar mProgressBar;

    private FileAdapter mMusicAdapter;
    private File mParentAlbumFile;

    public MusicFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mContext = getContext();
        initUI();
        init();


        return view;
    }


    private void initUI() {
        mMusicList = new ArrayList<>();
        mRvMusicList.setLayoutManager(new LinearLayoutManager(getContext()));
        mMusicAdapter = new FileAdapter<MusicFile>(getActivity(), R.layout.item_music_rv, mMusicList);
        mRvMusicList.setAdapter(mMusicAdapter);


    }

    private void init() {


        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mParentAlbumFile = new File(Environment.getExternalStorageDirectory(), Constant.ALBUM_IMG_PATH);
            if (!mParentAlbumFile.exists()) {
                mParentAlbumFile.mkdirs();
            }
        }
        mLoaderManager = getActivity().getLoaderManager();
        mLoaderManager.initLoader(LOAD_MUSIC, null, MusicFragment.this);


    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        Log.i("chk","onCreateLoader->"+ Thread.currentThread().getName());

        if (id == LOAD_MUSIC) {

            System.out.println("onCreateLoader");

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            String[] projections = new String[]{
                    MediaStore.Audio.Media.TITLE,  //音乐名
                    MediaStore.Audio.Media.ARTIST, // 艺术家
                    MediaStore.Audio.Media.DATA, //音乐文件所在路径
                    MediaStore.Audio.Media.ALBUM_ID, // 音乐封面
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DURATION //音乐时长
            };

            return new CursorLoader(getContext(), uri, projections, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");


        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {

        Log.i("chk","onLoadFinished->"+ Thread.currentThread().getName());

        System.out.println("onLoadFinished");

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
            mMusicList.add(fileInfo);


        }

        if (mMusicList.size() != 0) {
            if (mMusicList.size() == 0) {
                Toast.makeText(getContext(), "没有扫描到本地音乐", Toast.LENGTH_SHORT).show();
            }

            // 数据加载完成后隐藏进进度提示
            mProgressBar.setVisibility(View.INVISIBLE);
            // 刷新数据
            mMusicAdapter.notifyDataSetChanged();
            // 在线程中更新音乐封面图片
            updateAlbumImg();
        }


    }

    /**
     * 更新封面图片
     */
    public void updateAlbumImg() {

        MusicUtils.writeAlbumImg2local(getContext(), mParentAlbumFile, mMusicList);

    }

    @Override
    public void onLoaderReset(Loader loader) {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoaderManager.destroyLoader(LOAD_MUSIC);
        mUnbinder.unbind();
    }

}
