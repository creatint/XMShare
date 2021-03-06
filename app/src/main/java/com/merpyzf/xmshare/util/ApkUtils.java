package com.merpyzf.xmshare.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.util.FileUtils;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Const;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangke on 2017/12/25.
 */

public class ApkUtils {


    /**
     * 获取本地已安装的第三方APK的信息
     *
     * @param mPackageManager
     * @return
     */
    public static List<ApkFile> getApp(Activity context, PackageManager mPackageManager) {
        long start = System.currentTimeMillis();

        List<ApkFile> apkFileList = new ArrayList<>();

        List<ApplicationInfo> appList = mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (ApplicationInfo appInfo : appList) {


            //获取当前设备中的第三方的app
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {

                try {
                    ApkFile app = new ApkFile();
                    //获取app的name
                    String appName = (String) mPackageManager.getApplicationLabel(appInfo);
                    //获取app的图标
                    Drawable appIco = mPackageManager.getApplicationIcon(appInfo);
                    String appSourcePath = context.getApplication().getPackageManager().getApplicationInfo(appInfo.packageName, 0).sourceDir;
                    File file = new File(appSourcePath);
                    long length = file.length();

                    app.setApkDrawable(appIco);
                    app.setName(appName);
                    app.setPath(appSourcePath);
                    app.setLength((int) length);
                    app.setSuffix("apk");
                    app.setType(FileInfo.FILE_TYPE_APP);
                    apkFileList.add(app);


                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();

                }


            }
        }

        long end = System.currentTimeMillis();

        Log.i("wk", "扫描本地安装应用总共耗时:" + (end - start) / 1000f);


        return apkFileList;
    }


    /**
     * 获取Drawable实际占用大小
     *
     * @param drawable
     * @return
     */
    public static int getDrawableSize(Drawable drawable) {

        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);

//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.fav_jpg);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int len = baos.toByteArray().length;


        return len;
    }


    /**
     * 通过包名获取路径/data/app/ **.apk
     *
     * @param packageName
     * @return
     */
    public static String getApkFilePath(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 获取apk的图标信息
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }

    /**
     * 缓存apk的ico
     */
    public static void asyncCacheApkIco(Context context, List<ApkFile> apkList) {
        Observable.fromIterable(apkList)
                .filter(fileInfo -> {


                        if (Const.PIC_CACHES_DIR.canWrite() && !isContain(Const.PIC_CACHES_DIR, fileInfo)) {
                            return true;
                        }

                    return false;

                }).flatMap(fileInfo -> Observable.just(((ApkFile) fileInfo)))
                .subscribeOn(Schedulers.io())
                .subscribe(apkFile -> {

                    Bitmap bitmap = FileUtils.drawableToBitmap(apkFile.getApkDrawable());
                    BufferedOutputStream bos = null;
                    try {

                        File extPicCacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                        bos = new BufferedOutputStream(new FileOutputStream(new File(extPicCacheDir, Md5Utils.getMd5(apkFile.getName()))));
                        if (bitmap == null) {

                            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_thumb_empty);
                        }
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

                        Log.i("wk", apkFile.getName() + "--> 向缓存中写入ico");

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                });
    }


    /**
     * 判断缓存中是否已经存在
     *
     * @param parent
     * @param apkFile
     * @return
     */
    private static synchronized boolean isContain(File parent, ApkFile apkFile) {
        String[] icos = parent.list();
        for (int i = 0; i < icos.length; i++) {
            if (Md5Utils.getMd5(apkFile.getName()).equals(icos[i])) {
                return true;
            }
        }
        return false;
    }


}
