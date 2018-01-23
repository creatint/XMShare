package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.base.App;
import com.merpyzf.xmshare.ui.adapter.FileSelectAdapter;
import com.merpyzf.xmshare.ui.view.fragment.FileListFragment;
import com.merpyzf.xmshare.ui.widget.ApplyPermissionFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SelectFilesActivity extends AppCompatActivity {


    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.bottom_sheet)
    View mBottomSheet;
    @BindView(R.id.tool_bar)
    android.support.v7.widget.Toolbar mToolBar;
    @BindView(R.id.tv_bottom_title)
    TextView mTvBottomTitle;
    @BindView(R.id.rv_selected)
    RecyclerView mRvSelectedList;
    @BindView(R.id.fab_send)
    FloatingActionButton mFabSend;

    private Context mContext;
    private Unbinder mUnbinder;
    private List<Fragment> mFragmentList;
    private String[] mTabTitles;
    private BottomSheetBehavior<View> mSheetBehavior;
    private OnFileSelectListener<FileInfo> mFileSelectListener;
    private FileSelectAdapter<FileInfo> mFileSelectAdapter;


    public static void start(Context context) {

        context.startActivity(new Intent(context, SelectFilesActivity.class));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        mUnbinder = ButterKnife.bind(this);
        mContext = this;
        setSupportActionBar(mToolBar);
        initUI();
        initEvent();

        mFileSelectListener = new OnFileSelectListener<FileInfo>() {
            @Override
            public void onSelected(FileInfo fileInfo) {

                Log.i("w2k", "文件被选择了 --> " + fileInfo.getName());
                App.addSendFile(fileInfo);
                mFileSelectAdapter.notifyDataSetChanged();
                updateBottomTitle();


            }

            @Override
            public void onCancelSelected(FileInfo fileInfo) {

                Log.i("w2k", "文件被取消选择了 --> " + fileInfo.getName());
                App.removeSendFile(fileInfo);
                mFileSelectAdapter.notifyDataSetChanged();
                updateBottomTitle();

            }
        };


        // 判断是否要进行权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ApplyPermissionFragment applyPermissionFragment = new ApplyPermissionFragment();
            applyPermissionFragment.haveAll(getSupportFragmentManager(), this, new ApplyPermissionFragment.onApplyPermissionCompleted() {
                @Override
                public void onCompleted() {

                    Log.i("w2k", "权限授权完毕的回调");
                    init();

                }
            });
        } else {
            init();
        }


    }

    /**
     * 初始化UI
     */
    private void initUI() {

        updateBottomTitle();

        mRvSelectedList.setLayoutManager(new LinearLayoutManager(mContext));

        mFileSelectAdapter = new FileSelectAdapter<>(mContext, R.layout.item_rv_select, App.getSendFileList());

        //        mFileSelectAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
//        mFileSelectAdapter.isFirstOnly(false);
        mRvSelectedList.setAdapter(mFileSelectAdapter);


        mSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        // TODO: 2018/1/16 可能会出现OOM,使用ViewPager中Fragment的懒加载来解决ViewPager在快速切换时造成的卡顿问题
        mViewPager.setOffscreenPageLimit(4);

    }

    /**
     * 更新底部BottomSheet的标题
     */
    public void updateBottomTitle() {

        mTvBottomTitle.setText("选择文件的个数: " + App.getSendFileList().size());
    }

    /**
     * 初始化页面相关的数据
     */
    private void init() {

        mFragmentList = new ArrayList<>();
        mTabTitles = new String[4];

        mTabTitles[0] = "应用";
        mTabTitles[1] = "图片";
        mTabTitles[2] = "音乐";
        mTabTitles[3] = "视频";

        // 应用
        FileListFragment appFragment = FileListFragment.newInstance(FileInfo.FILE_TYPE_APP, mFileSelectListener);
        mFragmentList.add(appFragment);

        // 图片
        FileListFragment picFragment = FileListFragment.newInstance(FileInfo.FILE_TYPE_IMAGE, mFileSelectListener);
        mFragmentList.add(picFragment);

        // 音乐
        FileListFragment musicFragment = FileListFragment.newInstance(FileInfo.FILE_TYPE_MUSIC, mFileSelectListener);
        mFragmentList.add(musicFragment);

        // 视频
        FileListFragment videoFragment = FileListFragment.newInstance(FileInfo.FILE_TYPE_VIDEO, mFileSelectListener);
        mFragmentList.add(videoFragment);


        MyFragmentPagerAdapter frgPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList, mTabTitles);
        mViewPager.setAdapter(frgPagerAdapter);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.setTabsFromPagerAdapter(frgPagerAdapter);

    }


    /**
     * 初始化事件
     */
    private void initEvent() {

        mSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // 禁止BotttomSheet滑动
                // mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mFabSend.setOnClickListener(v -> {

            Log.i("w2k", "开始发送文件");
//            TransferSendActivity.start(mContext);
            SendActivity.start(mContext);

        });

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

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);

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
