package com.merpyzf.xmshare.ui.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.view.fragment.FileListFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SelectFilesActivity extends AppCompatActivity {

    private Unbinder mUnbinder;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.bottom_sheet)
    View mBottomSheet;

    private List<Fragment> mFragmentList;
    private String[] mTabTitles;
    private BottomSheetBehavior<View> mSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        mUnbinder = ButterKnife.bind(this);

        initUI();
        init();
        MyFragmentPagerAdapter frgPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList, mTabTitles);
        mViewPager.setAdapter(frgPagerAdapter);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.setTabsFromPagerAdapter(frgPagerAdapter);


    }

    private void initUI() {

        mSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        mSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // 禁止BotttomSheet滑动
//                mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    private void init() {

        mFragmentList = new ArrayList<>();
        mTabTitles = new String[3];


        FileListFragment appFragment = new FileListFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt("load_file_type", FileInfo.FILE_TYPE_APP);
        appFragment.setArguments(bundle1);
        mFragmentList.add(appFragment);


        FileListFragment picFragment = new FileListFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putInt("load_file_type", FileInfo.FILE_TYPE_IMAGE);
        picFragment.setArguments(bundle2);
        mFragmentList.add(picFragment);

        FileListFragment musicFragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("load_file_type", FileInfo.FILE_TYPE_MUSIC);
        musicFragment.setArguments(bundle);
        mFragmentList.add(musicFragment);




        mTabTitles[0] = "应用";
        mTabTitles[1] = "图片";
        mTabTitles[2] = "音乐";

    }


    // TODO: 2018/1/9  Fragment的适配器需要抽离到外部
    class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> mFragmentList;
        private String[] mTabTitles;

        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] tabTitles) {
            super(fm);
            this.mFragmentList = fragmentList;
            this.mTabTitles = tabTitles;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
    }
}
