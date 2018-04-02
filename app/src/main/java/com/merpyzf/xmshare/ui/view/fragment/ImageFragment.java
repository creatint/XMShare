package com.merpyzf.xmshare.ui.view.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.merpyzf.filemanager.widget.FileSelectIndicator;
import com.merpyzf.filemanager.widget.bean.Label;
import com.merpyzf.xmshare.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {



    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.checkbox_all)
    CheckBox mCheckbox;
    @BindView(R.id.fileSelectIndicator)
    FileSelectIndicator mFileSelectIndicator;
    private Unbinder mUnbinder;


    public ImageFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_image, container, false);


        mUnbinder = ButterKnife.bind(this, rootView);

        initUI();


        return rootView;
    }

    private void initUI() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_container, new ImgDirsFragment());
        fragmentTransaction.commit();

        mFileSelectIndicator.add(new Label("数字尾巴", ""));

    }

    public TextView getTvTitle() {
        return mTvTitle;
    }

    public CheckBox getCheckbox() {
        return mCheckbox;
    }

    public FileSelectIndicator getFileSelectIndicator() {
        return mFileSelectIndicator;
    }



    @Override
    public void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }
}
