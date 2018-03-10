package com.merpyzf.xmshare.bean.model;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Default;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by merpyzf on 2018/3/10.
 * 用于存放本机文件的M5d值
 */
@Table("file_md5_model")
public class FileMd5Model {


    public FileMd5Model(String fileName, String md5) {
        this.fileName = fileName;
        this.md5 = md5;
    }

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    // 文件名非空
    @Default("true")
    @Column("file_name")
    @NotNull
    private String fileName;

    // md5非空
    @Default("true")
    @Column("file_md5")
    @NotNull
    private String md5;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
