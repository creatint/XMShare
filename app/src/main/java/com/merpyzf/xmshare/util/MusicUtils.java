package com.merpyzf.xmshare.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Const;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangke on 2017/12/23.
 */

public class MusicUtils {

    /**
     * 从媒体库加载封面
     */
    public static Bitmap loadCoverFromMediaStore(Context context, long albumId) {

        ContentResolver resolver = context.getContentResolver();
        Uri uri = getMediaStoreAlbumCoverUri(albumId);
        InputStream is;
        try {
            is = resolver.openInputStream(uri);
        } catch (FileNotFoundException ignored) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * 获取音乐封面图的uri
     *
     * @param albumId
     * @return
     */
    public static Uri getMediaStoreAlbumCoverUri(long albumId) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

    /**
     * 将专辑封面图缓存到本地
     *
     * @param context
     * @param musicList
     */
    public static synchronized void writeAlbumImg2local(final Context context, List<FileInfo> musicList) {


        Observable.fromIterable(musicList)
                .filter(musicFile -> {

                    if (musicFile instanceof MusicFile) {

                        if (Const.PIC_CACHES_DIR.canWrite() && !isContain(Const.PIC_CACHES_DIR, (MusicFile) musicFile)) {
                            return true;
                        }
                    }
                    return false;

                }).flatMap(musicFile -> Observable.just(((MusicFile) musicFile))).subscribeOn(Schedulers.io())
                .subscribe(new Observer<MusicFile>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MusicFile musicFile) {

                        Bitmap bitmap = loadCoverFromMediaStore(context, musicFile.getAlbumId());
                        BufferedOutputStream bos = null;
                        try {

                            File extPicCacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                            bos = new BufferedOutputStream(new FileOutputStream(new File(extPicCacheDir, Md5Utils.getMd5(musicFile.getAlbumId()+""))));
                            if (bitmap == null) {

                                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_thumb_empty);
                            }
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

                            Log.i("wk", musicFile.getName() + "--> 向缓存中写入图片");

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                bos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }


    /**
     * 判断要获取到音乐封面是否在本地已经存在
     *
     * @param musicFile
     * @return true
     * false 不包含
     */
    private static synchronized boolean isContain(File parent, MusicFile musicFile) {
        String[] albums = parent.list();
        for (int i = 0; i < albums.length; i++) {
            if (Md5Utils.getMd5(musicFile.getAlbumId()+"").equals(albums[i])) {
                return true;
            }
        }
        // TODO: 2017/12/24 考虑增加清理音乐文件不存在的album_id
        return false;
    }


    /**
     * 更新封面图片
     */
    public static synchronized void updateAlbumImg(Context context, List<FileInfo> fileInfoList) {

        // copy一份List<FileInfo>到一个新的集合避免在使用iterator遍历集合的同时又对集合修改，就会产生ConcurrentModificationException

        List<FileInfo> copyFileInfoList = new ArrayList<>();
        copyFileInfoList.addAll(fileInfoList);
        writeAlbumImg2local(context, copyFileInfoList);

    }
}
