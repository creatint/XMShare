package com.merpyzf.xmshare.ui.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.ui.view.fragment.APPFragment;
import com.merpyzf.xmshare.ui.view.fragment.MusicFragment;

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

    private List<Fragment> mFragmentList;
    private String[] mTabTitles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        mUnbinder = ButterKnife.bind(this);

        init();

        MyFragmentPagerAdapter frgPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList,mTabTitles);
        mViewPager.setAdapter(frgPagerAdapter);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.setTabsFromPagerAdapter(frgPagerAdapter);


    }

    private void init() {

        mFragmentList = new ArrayList<>();
        mTabTitles = new String[2];
        mFragmentList.add(new APPFragment());
        mFragmentList.add(new MusicFragment());

        mTabTitles[0] = "应用";
        mTabTitles[1] = "音乐";

    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter{

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
