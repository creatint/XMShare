package com.merpyzf.xmshare.ui.test;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.merpyzf.filemanager.widget.FileSelectIndicator;
import com.merpyzf.filemanager.widget.bean.Label;
import com.merpyzf.xmshare.R;

public class TestFileIndicatorActivity extends AppCompatActivity {

    private FileSelectIndicator mFileSelectIndicator;
    private Button mBtnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_file_indicator);
        mBtnAdd = findViewById(R.id.btn_add);

        String path = Environment.getExternalStorageDirectory().getPath();

        Log.i("wk", path);

        mFileSelectIndicator = findViewById(R.id.fileSelectIndicator);


        mFileSelectIndicator.add(new Label("内部文件存储", Environment.getExternalStorageDirectory().getParent()));

        final int[] num = {2};

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mFileSelectIndicator.add(new Label("wangke"+(num[0]++), "wangke"));


            }
        });


        mFileSelectIndicator.setIndicatorClickCallBack((currentPath, isBack) -> {

            Log.i("wk", "当前点击的路径 -> "+currentPath);


            if(isBack){

                Log.i("wk", "返回到上一个界面");

            }




        });
    }


}
