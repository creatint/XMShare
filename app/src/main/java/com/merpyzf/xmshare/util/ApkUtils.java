package com.merpyzf.xmshare.util;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.merpyzf.xmshare.ui.entity.ApkFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                    float appSize = length / (1024 * 1f) / 1024;
                    Log.i("wk", "appName: " + appName + " appSize:" + appSize);

                    app.setApkDrawable(appIco);
                    app.setName(appName);
                    app.setPath(appSourcePath);
                    app.setSize(length);
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
     * @param drawable
     * @return
     */
    public static int getDrawableSize(Drawable drawable){

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


}