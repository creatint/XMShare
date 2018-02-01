package com.merpyzf.xmshare.ui.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.merpyzf.httpcoreserver.ui.HttpServerActivity;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.common.base.App;
import com.merpyzf.xmshare.ui.adapter.FileSelectAdapter;
import com.merpyzf.xmshare.ui.view.fragment.FileListFragment;
import com.merpyzf.xmshare.ui.view.interfaces.PersonalObservable;
import com.merpyzf.xmshare.ui.view.interfaces.PersonalObserver;
import com.merpyzf.xmshare.ui.widget.ApplyPermissionFragment;
import com.merpyzf.xmshare.util.SharedPreUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class SelectFilesActivity extends AppCompatActivity implements PersonalObserver {


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
    @BindView(R.id.linear_menu)
    LinearLayout mLinearMenu;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    // toolbar上的昵称
    @BindView(R.id.tv_nickname)
    TextView mTvNickname;
    // toolbar上的头像
    @BindView(R.id.civ_avatar)
    CircleImageView mCivAvatar;


    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    CircleImageView mNavCivAvatar;
    // 昵称
    TextView mNavTvNickname;


    private Context mContext;
    private Unbinder mUnbinder;
    private List<Fragment> mFragmentList;
    private String[] mTabTitles;
    private BottomSheetBehavior<View> mSheetBehavior;
    private OnFileSelectListener<FileInfo> mFileSelectListener;
    private FileSelectAdapter<FileInfo> mFileSelectAdapter;
    private String TAG = SelectFilesActivity.class.getSimpleName();

    public static void start(Context context) {

        context.startActivity(new Intent(context, SelectFilesActivity.class));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        mUnbinder = ButterKnife.bind(this);
        mContext = this;



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

            /**
             * 全选/取消全选的回调
             */
            @Override
            public void onCheckedAll() {
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

        View headerView = mNavigationView.getHeaderView(0);
        mNavCivAvatar = headerView.findViewById(R.id.civ_avatar);
        mNavTvNickname = headerView.findViewById(R.id.tv_nickname);

        // 更新头像和昵称
        update();
        updateBottomTitle();
        mRvSelectedList.setLayoutManager(new LinearLayoutManager(mContext));

        mFileSelectAdapter = new FileSelectAdapter<>(mContext, R.layout.item_rv_select, App.getSendFileList());

        mRvSelectedList.setAdapter(mFileSelectAdapter);


        mSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        // TODO: 2018/1/16 可能会出现OOM,使用ViewPager中Fragment的懒加载来解决ViewPager在快速切换时造成的卡顿问题
        mViewPager.setOffscreenPageLimit(4);

    }

    /**
     * 更新底部BottomSheet的标题
     */
    public void updateBottomTitle() {

        if (App.getSendFileList().size() == 0) {
            mTvBottomTitle.setText("请选择要传输的文件");
            return;
        }
        mTvBottomTitle.setText("已选文件个数: " + App.getSendFileList().size());
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

        PersonalObservable.getInstance().register(this);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // 如果为选择文件则进制BottomSheet的滑动
                if (App.getSendFileList().size() == 0) {
                    mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        // 头像的点击事件
        mNavCivAvatar.setOnClickListener(v -> {
            PersonalActivity.start(mContext);
        });

        mFabSend.setOnClickListener(v -> {

            if (App.getSendFileList().size() > 0) {
                Log.i("w2k", "开始发送文件");
                SendActivity.start(mContext);
            } else {
                Toast.makeText(mContext, "请选择文件", Toast.LENGTH_SHORT).show();
            }

        });

        // 顶部menu按钮
        mLinearMenu.setOnClickListener(v -> {

            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.END);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 导航栏菜单的点击事件
        mNavigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            switch (id) {

                // 接收文件
                case R.id.nav_receive:
                    ReceiveActivity.start(mContext);
                    break;
                // 电脑传
                case R.id.nav_transfer2pc:

                    HttpServerActivity.start(mContext);

                    break;

                // 邀请安装
                case R.id.nav_invite:

                    InviteActivity.start(mContext);

                    break;

                // 设置
                case R.id.nav_setting:

                    SettingActivity.start(mContext);

                    break;

                // 分享
                case R.id.nav_share:
                    break;


                // 检查新版本
                case R.id.nav_update:

                   break;

                // 反馈
                case R.id.nav_feedback:


                    break;

            }

            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }


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

    // 头像/昵称发生变化时的回调
    @Override
    public void update() {

        System.out.println("update执行了");
        mNavTvNickname.setText(SharedPreUtils.getNickName(mContext));
        setAvatar(mNavCivAvatar, Constant.AVATAR_LIST.get(SharedPreUtils.getAvatar(mContext)));
        setAvatar(mCivAvatar, Constant.AVATAR_LIST.get(SharedPreUtils.getAvatar(mContext)));




    }

    // 设置头像
    private void setAvatar(CircleImageView view, int avatar) {
        // 设置头像
        Glide.with(mContext)
                .load(avatar)
                .crossFade()
                .centerCrop()
                .into(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        // 取消注册，从观察者集合中移除
        PersonalObservable.getInstance().unRegister(this);
        App.getSendFileList().clear();
    }
}
