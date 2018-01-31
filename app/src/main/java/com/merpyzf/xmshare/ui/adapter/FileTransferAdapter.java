package com.merpyzf.xmshare.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.merpyzf.transfermanager.constant.Constant;
import com.merpyzf.transfermanager.entity.ApkFile;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.transfermanager.entity.MusicFile;
import com.merpyzf.transfermanager.entity.PicFile;
import com.merpyzf.transfermanager.entity.VideoFile;
import com.merpyzf.transfermanager.interfaces.TransferObserver;
import com.merpyzf.transfermanager.receive.ReceiverManager;
import com.merpyzf.transfermanager.send.SenderManager;
import com.merpyzf.transfermanager.util.FileUtils;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.App;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

/**
 * Created by wangke on 2018/1/18.
 * 文件传输列表
 */

public class FileTransferAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<FileInfo> mFileLists;
    private ImageView mIvThumb;
    public static final int TYPE_SEND = 1;
    public static final int TYPE_RECEIVE = 2;
    private int type = -1;


    public FileTransferAdapter(int layoutResId, int type, @Nullable List<T> data) {
        super(layoutResId, data);
        this.type = type;
        this.mFileLists = (List<FileInfo>) data;
    }

    @Override
    protected void convert(BaseViewHolder helper, T item) {

        FileInfo file = (FileInfo) item;
        String fileName = file.getName();
        ImageView ivThumb = helper.getView(R.id.iv_file_thumb);
        ImageView ivDone = helper.getView(R.id.iv_done);
        // 设置传输的文件的大小
        TextView tvSize = helper.getView(R.id.tv_size);
        int length = file.getLength();
        String[] fileSizeArrayStr = FileUtils.getFileSizeArrayStr(length);
        tvSize.setText(fileSizeArrayStr[0] + fileSizeArrayStr[1]);


        Bitmap bitmap = null;

        if (item instanceof ApkFile) {

            ApkFile apkFile = (ApkFile) item;
            bitmap = FileUtils.drawableToBitmap(apkFile.getApkDrawable());


        } else if (item instanceof MusicFile) {

            MusicFile musicFile = (MusicFile) item;
            String musicThumbPath = Environment.getExternalStorageDirectory().getPath() + Constant.THUMB_MUSIC + "/" + String.valueOf(musicFile.getAlbumId());
            bitmap = BitmapFactory.decodeFile(musicThumbPath);


        } else if (item instanceof PicFile) {

            PicFile picFile = (PicFile) item;

            if (FileTransferAdapter.TYPE_SEND == type) {

                //设置封面图片
                Glide.with(mContext)
                        .load(picFile.getPath())
                        .placeholder(R.drawable.ic_thumb_empty)
                        .crossFade()
                        .centerCrop()
                        .error(R.drawable.ic_header)
                        .into(ivThumb);

            }

        } else if (item instanceof VideoFile) {

            VideoFile videoFile = (VideoFile) item;
            String videoThumbPath = Environment.getExternalStorageDirectory()
                    + Constant.THUMB_VIDEO + "/" + videoFile.getName();

            bitmap = BitmapFactory.decodeFile(videoThumbPath);

        }

        TextView tvTitle = helper.getView(R.id.tv_title);
        tvTitle.setText(file.getName());


        File thumbFile = new File(Environment.getExternalStorageDirectory().getPath()
                + com.merpyzf.transfermanager.constant.Constant.THUMB_RECEIVE, file.getName());


        if (FileTransferAdapter.TYPE_RECEIVE == type) {
            //设置封面图片
            Glide.with(mContext)
                    .load(thumbFile)
                    .placeholder(R.drawable.ic_thumb_empty)
                    .crossFade()
                    .centerCrop()
                    .error(R.drawable.ic_header)
                    .into(ivThumb);

        } else if (FileTransferAdapter.TYPE_SEND == type) {

            if (bitmap != null) {
                //设置封面图片
                Glide.with(mContext)
                        .load(FileUtils.bitmapToByteArray(bitmap))
                        .placeholder(R.drawable.ic_thumb_empty)
                        .crossFade()
                        .centerCrop()
                        .error(R.drawable.ic_header)
                        .into(ivThumb);
            }
        }


        ProgressBar progressBar = helper.getView(R.id.progress);

        TextView tvProgress = helper.getView(R.id.tv_progress);

        TextView tvSpeed = helper.getView(R.id.tv_speed);


        if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_WAITING) {

            progressBar.setProgress(0);
            tvProgress.setText("等待中");
            tvSpeed.setVisibility(View.INVISIBLE);
            ivDone.setVisibility(View.INVISIBLE);
        }

        if (type == TYPE_RECEIVE) {
            ReceiverManager.getInstance().register(new TransferObserver() {

                @Override
                public void onTransferProgress(FileInfo fileInfo) {


                    if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFING) {

                        if (fileName.equals(fileInfo.getName())) {

                            // 如果可见设置为不可见
                            if (ivDone.getVisibility() == View.VISIBLE) {
                                ivDone.setVisibility(View.INVISIBLE);
                            }

                            // 如果进度条不可见则设置为可见
                            if (progressBar.getVisibility() == View.INVISIBLE) {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                            int currentProgress = (int) (file.getProgress() * 100);
                            progressBar.setProgress(currentProgress);
                            tvProgress.setText("传输进度:" + currentProgress + " %");
                            String[] transferSpeed = file.getTransferSpeed();
                            if (transferSpeed != null && transferSpeed.length > 0) {

                                if (tvSpeed.getVisibility() == View.INVISIBLE) {
                                    tvSpeed.setVisibility(View.VISIBLE);
                                }


                                tvSpeed.setText("速度: " + transferSpeed[0] + transferSpeed[1] + " /s");
                            }
                        }

                    } else if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_WAITING) {

                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar.setProgress(0);
                        tvProgress.setText("等待中");
                        ivDone.setVisibility(View.INVISIBLE);
                        tvSpeed.setVisibility(View.INVISIBLE);

                    } else if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_SUCCESS) {
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.INVISIBLE);
                        ivDone.setVisibility(View.VISIBLE);
                        tvSpeed.setVisibility(View.INVISIBLE);
                        tvProgress.setText("传输完毕，" + getOpenTypeText(fileInfo));


                    }

                }

                @Override
                public void onTransferStatus(FileInfo fileInfo) {

                    if (fileInfo.getName().equals(file.getName())) {


                        if (fileInfo.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_SUCCESS) {

                            tvProgress.setText("传输完毕，" + getOpenTypeText(fileInfo));
                            progressBar.setProgress(100);
                            progressBar.setVisibility(View.INVISIBLE);
                            tvSpeed.setVisibility(View.INVISIBLE);
                            ivDone.setVisibility(View.VISIBLE);

                        } else if (fileInfo.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_FAILED) {

                            tvProgress.setText("传输失败");
                            tvSpeed.setVisibility(View.INVISIBLE);
                        }
                    }
                }

            });
        } else if (type == TYPE_SEND) {
            SenderManager.getInstance(mContext).register(new TransferObserver() {
                @Override
                public void onTransferProgress(FileInfo fileInfo) {


                    if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFING) {

                        if (fileName.equals(fileInfo.getName())) {

                            // 如果可见设置为不可见
                            if (ivDone.getVisibility() == View.VISIBLE) {
                                ivDone.setVisibility(View.INVISIBLE);
                            }

                            // 如果进度条不可见则设置为可见
                            if (progressBar.getVisibility() == View.INVISIBLE) {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                            int currentProgress = (int) (file.getProgress() * 100);
                            progressBar.setProgress(currentProgress);
                            tvProgress.setText("传输进度:" + currentProgress + " %");
                            String[] transferSpeed = file.getTransferSpeed();
                            if (transferSpeed != null && transferSpeed.length > 0) {

                                if (tvSpeed.getVisibility() == View.INVISIBLE) {
                                    tvSpeed.setVisibility(View.VISIBLE);
                                }


                                tvSpeed.setText("速度: " + transferSpeed[0] + transferSpeed[1] + " /s");
                            }
                        }

                    } else if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_WAITING) {

                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar.setProgress(0);
                        tvProgress.setText("等待中");
                        ivDone.setVisibility(View.INVISIBLE);
                        tvSpeed.setVisibility(View.INVISIBLE);

                    } else if (file.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_SUCCESS) {
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.INVISIBLE);
                        ivDone.setVisibility(View.VISIBLE);
                        tvSpeed.setVisibility(View.INVISIBLE);
                        tvProgress.setText("传输完毕");


                    }

                }

                @Override
                public void onTransferStatus(FileInfo fileInfo) {

                    if (fileInfo.getName().equals(file.getName())) {


                        if (fileInfo.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_SUCCESS) {

                            tvProgress.setText("传输完毕");
                            progressBar.setProgress(100);
                            progressBar.setVisibility(View.INVISIBLE);
                            tvSpeed.setVisibility(View.INVISIBLE);
                            ivDone.setVisibility(View.VISIBLE);

                            // 文件全部传输成功之后重置待传输文件的状态
                            if (fileInfo.getIsLast() == 1) {
                                App.resetSendFileList();
                            }

                        } else if (fileInfo.getFileTransferStatus() == Constant.TransferStatus.TRANSFER_FAILED) {

                            tvProgress.setText("传输失败");
                            tvSpeed.setVisibility(View.INVISIBLE);
                        }
                    }
                }


            });
        }


    }


    @NonNull
    @Override
    public String getSectionName(int position) {
        return mFileLists.get(position).getName().substring(0, 1);
    }


    public String getOpenTypeText(FileInfo fileInfo) {

        String typeText = null;

        switch (fileInfo.getType()) {
            case FileInfo.FILE_TYPE_APP:
                typeText = "点击安装";
                break;
            case FileInfo.FILE_TYPE_MUSIC:
            case FileInfo.FILE_TYPE_VIDEO:
                typeText = "点击播放";
                break;
            case FileInfo.FILE_TYPE_IMAGE:
                typeText = "点击查看";
                break;
            default:
                break;

        }

        return typeText;

    }
}