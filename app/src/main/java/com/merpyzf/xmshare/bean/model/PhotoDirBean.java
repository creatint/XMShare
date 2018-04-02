package com.merpyzf.xmshare.bean.model;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by merpyzf on 2018/4/2.
 */

public class PhotoDirBean {

    // 相册文件夹的封面图
    private String coverImg;
    // 相册名
    private String name;
    // 是否选中
    private boolean isChecked = false;

    private List<FileInfo> imageList;


    public PhotoDirBean(String coverImg, String name, boolean isChecked, List<FileInfo> imageList) {
        this.coverImg = coverImg;
        this.name = name;
        this.isChecked = isChecked;
        this.imageList = imageList;
    }

    public PhotoDirBean() {
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public List<FileInfo> getImageList() {
        return imageList;
    }

    public void setImageList(List<FileInfo> imageList) {
        this.imageList = imageList;
    }


    public void setImageList(File[] images) {


        List<FileInfo> imageList = new ArrayList<>();


        for (File image : images) {

            FileInfo fileInfo = new FileInfo();
            fileInfo.setPath(image.getPath());
            fileInfo.setName(image.getName());
            fileInfo.setLength((int) image.length());
            fileInfo.setSuffix(FileUtils.getFileSuffix(image));
            fileInfo.setType(FileInfo.FILE_TYPE_IMAGE);
            imageList.add(fileInfo);

        }


        this.imageList = imageList;



    }




    /**
     * 返回一个相册照片的数量
     *
     * @return
     */
    public int getImageNumber() {

        if (imageList == null) {
            return 0;
        }
        return imageList.size();

    }
}
