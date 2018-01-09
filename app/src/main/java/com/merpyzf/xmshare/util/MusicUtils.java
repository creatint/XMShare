package com.merpyzf.xmshare.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.entity.MusicFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
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


    public static void writeAlbumImg2local(final Context context, final File parent, List<FileInfo> musicList) {


        Observable.fromIterable(musicList)
                .filter(new Predicate<FileInfo>() {
                    @Override
                    public boolean test(FileInfo musicFile) throws Exception {

                        if (musicFile instanceof MusicFile) {

                            if (parent.canWrite() && !isContain(parent, (MusicFile) musicFile)) {
                                return true;
                            }
                        }
                        return false;

                    }
                }).flatMap(new Function<FileInfo, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(FileInfo musicFile) throws Exception {
                return Observable.just(((MusicFile)musicFile).getAlbumId());
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long albumId) throws Exception {
                        Bitmap bitmap = loadCoverFromMediaStore(context, albumId);
                        BufferedOutputStream bos = null;
                        try {
                            bos = new BufferedOutputStream(new FileOutputStream(new File(parent,
                                    "" + albumId)));
                            if (bitmap == null) {

                                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_album_empty);
                            }
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
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
            if (musicFile.getAlbumId() == Integer.valueOf(albums[i])) {
                return true;
            }
        }
        // TODO: 2017/12/24 考虑增加清理音乐文件不存在的album_id
        return false;
    }

}
