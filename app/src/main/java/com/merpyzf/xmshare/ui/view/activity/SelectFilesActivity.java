package com.merpyzf.xmshare.ui.view.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.merpyzf.filemanager.fragment.MainFragment;
import com.merpyzf.httpcoreserver.ui.HttpServerActivity;
import com.merpyzf.transfermanager.entity.FileInfo;
import com.merpyzf.xmshare.R;
import com.merpyzf.xmshare.App;
import com.merpyzf.xmshare.common.Constant;
import com.merpyzf.xmshare.common.base.BaseActivity;
import com.merpyzf.xmshare.ui.adapter.FileSelectAdapter;
import com.merpyzf.xmshare.ui.adapter.FilesFrgPagerAdapter;
import com.merpyzf.xmshare.ui.view.fragment.FileListFragment;
import com.merpyzf.xmshare.ui.view.interfaces.PersonalObservable;
import com.merpyzf.xmshare.ui.view.interfaces.PersonalObserver;
import com.merpyzf.xmshare.util.SharedPreUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.Consumer;

/**
 * 应用首页界面
 *
 * @author wangke
 */
public class SelectFilesActivity extends BaseActivity implements PersonalObserver {


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

    private List<Fragment> mFragmentList;
    private String[] mTabTitles;
    private BottomSheetBehavior<View> mSheetBehavior;
    private OnFileSelectListener<FileInfo> mFileSelectListener;
    private FileSelectAdapter<FileInfo> mFileSelectAdapter;
    private String TAG = SelectFilesActivity.class.getSimpleName();


    @Override
    public int getLayoutId() {
        return R.layout.activity_select_file;
    }

    @Override
    public void initRecyclerView() {

        mRvSelectedList.setLayoutManager(new LinearLayoutManager(mContext));
        mFileSelectAdapter = new FileSelectAdapter<>(mContext, R.layout.item_rv_select, App.getSendFileList());
        mRvSelectedList.setAdapter(mFileSelectAdapter);
    }

    @Override
    public void initViews(Bundle savedInstanceState) {


        View headerView = mNavigationView.getHeaderView(0);
        mNavCivAvatar = headerView.findViewById(R.id.civ_avatar);
        mNavTvNickname = headerView.findViewById(R.id.tv_nickname);

        // 更新头像和昵称
        update();
        updateBottomTitle();
        // 初始并显示SheetBottom中的Recycler
        initRecyclerView();
        mSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        FragmentManager fragmentManager = getSupportFragmentManager();
        // 申请权限
        new RxPermissions(SelectFilesActivity.this)
                .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {

                        // 用户已经同意给与该权限
                        if (permission.granted) {
                            // 加载ViewPager
                            FilesFrgPagerAdapter frgPagerAdapter = new FilesFrgPagerAdapter(fragmentManager, mFragmentList, mTabTitles);
                            mViewPager.setAdapter(frgPagerAdapter);
                            mTabs.setupWithViewPager(mViewPager);
                            mTabs.setTabsFromPagerAdapter(frgPagerAdapter);
                            mViewPager.setOffscreenPageLimit(4);

                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            Log.d(TAG, permission.name + " is denied. More info should be provided.");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            Log.d(TAG, permission.name + " is denied.");
                        }

                    }
                });


    }

    @Override
    protected void initData() {

        // 文件选择列表变化的监听
        mFileSelectListener = new OnFileSelectListener<FileInfo>() {
            /**
             * 选择文件的回调
             * @param fileInfo
             */
            @Override
            public void onSelected(FileInfo fileInfo) {

                App.addSendFile(fileInfo);
                mFileSelectAdapter.notifyDataSetChanged();
                updateBottomTitle();


            }

            /**
             * 文件取消选择的回调
             * @param fileInfo
             */
            @Override
            public void onCancelSelected(FileInfo fileInfo) {

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


        // 初始化主页要显示的可以传输文件的类别

        mFragmentList = new ArrayList<>();
        mTabTitles = new String[5];
        mTabTitles[0] = "文件";
        mTabTitles[1] = "应用";
        mTabTitles[2] = "图片";
        mTabTitles[3] = "音乐";
        mTabTitles[4] = "视频";


        // 文件
        mFragmentList.add(new MainFragment());
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




    }

    @Override
    public void initEvents() {

        // 注册一个用户昵称和头像变化的观察者
        PersonalObservable.getInstance().register(this);

        // BottomSheet的滑动中的回调事件
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

        // 浮动发送按钮的点击事件
        mFabSend.setOnClickListener(v -> {

            if (App.getSendFileList().size() > 0) {
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

                    SettingActivity.start(mContext, SettingActivity.class);

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
    protected void initToolBar() {

    }

    /**
     * 当头像和昵称发生变化时候的更新通知
     */
    @Override
    public void update() {

        mNavTvNickname.setText(SharedPreUtils.getNickName(mContext));
        setAvatar(mNavCivAvatar, Constant.AVATAR_LIST.get(SharedPreUtils.getAvatar(mContext)));
        setAvatar(mCivAvatar, Constant.AVATAR_LIST.get(SharedPreUtils.getAvatar(mContext)));


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
     * 设置头像
     *
     * @param view
     * @param avatar
     */
    private void setAvatar(CircleImageView view, int avatar) {
        // 设置头像
        Glide.with(mContext)
                .load(avatar)
                .crossFade()
                .centerCrop()
                .into(view);
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

    @Override
    protected void onDestroy() {
        // 取消注册，从观察者集合中移除
        PersonalObservable.getInstance().unRegister(this);
        App.getSendFileList().clear();
        super.onDestroy();


    }
}
