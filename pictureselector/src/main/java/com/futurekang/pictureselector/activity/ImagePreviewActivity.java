package com.futurekang.pictureselector.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.futurekang.pictureselector.R;
import com.futurekang.pictureselector.adapter.RCommAdapter;
import com.futurekang.pictureselector.anim.SimpleAnimationListener;
import com.futurekang.pictureselector.model.MediaFileInfo;
import com.futurekang.pictureselector.photoview.PhotoView;
import com.futurekang.pictureselector.tools.BitmapUtils;
import com.futurekang.pictureselector.tools.NavigationBarUtil;
import com.futurekang.pictureselector.tools.StatusBarUtil;
import com.futurekang.pictureselector.tools.ToastUtils;
import com.futurekang.pictureselector.view.FutureViewPager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.futurekang.pictureselector.activity.AlbumPreviewActivity.isOriginal;

public class ImagePreviewActivity extends AppCompatActivity {

    ImageButton ibBack;
    TextView tvTitle;
    Button btnSend;
    RelativeLayout actionBar;
    FutureViewPager vpPhoto;
    Button btnSetting;
    CheckBox tbSelectOriginal;
    CheckBox cbSelected;
    LinearLayout layoutBottomMenu;
    RecyclerView recyclerIndicator;
    ImageView ivVideoOpen;

    private PhotoViewAdapter photoViewAdapter;

    private RCommAdapter<MediaFileInfo> indicatorAdapter;

    private int maxSelectCount = 1;

    private List<MediaFileInfo> mediaFileInfos = null;
    private HashSet<MediaFileInfo> checkedPathList = null;
    private List<MediaFileInfo> indicatorList = new ArrayList<>();
    private MediaFileInfo currentFile;
    private int position = 0;

    private static int PREVIEW_CHECKED = 0x1111111;
    private static int PREVIEW_ALL = 0x222222;
    private String TAG = "ImagePreviewActivity";

    public static void startImagePreviewChecked(Activity activity, int position) {
        startImagePreviewActivity(activity, position, PREVIEW_CHECKED);
    }

    public static void startImagePreviewAll(Activity activity, int position) {
        startImagePreviewActivity(activity, position, PREVIEW_ALL);
    }

    public static void startImagePreviewActivity(Activity activity, int position, int flag) {
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle();
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("flag", flag);
        activity.startActivityForResult(intent, 10000, bundle);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        StatusBarUtil.setAppCompatWindowNoTitle(this);
        getWindow().setExitTransition(new Fade());//出去的动画
        getWindow().setEnterTransition(new Fade());//进来的动画
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        findView();
        setSourceData();
        initView();
        initImageAdapter();
        initIndicatorAdaper();
    }

    private void findView() {

        vpPhoto = findViewById(R.id.vp_photo);
        ivVideoOpen = findViewById(R.id.iv_video_open);
        actionBar = findViewById(R.id.action_bar);
        ibBack = findViewById(R.id.ib_back);
        tvTitle = findViewById(R.id.tv_title);
        btnSend = findViewById(R.id.btn_send);
        layoutBottomMenu = findViewById(R.id.layout_bottom_menu);
        btnSetting = findViewById(R.id.btn_setting);
        tbSelectOriginal = findViewById(R.id.tb_select_original);
        cbSelected = findViewById(R.id.cb_selected);
        recyclerIndicator = findViewById(R.id.recycler_Indicator);
    }

    private void initIndicatorAdaper() {
        indicatorAdapter = new RCommAdapter<MediaFileInfo>(indicatorList, R.layout.widget_image_indicator_item) {
            @Override
            public void setView(RCViewHolder viewHolder, int position, MediaFileInfo itemData) {
                ImageView imageView = viewHolder.getItemView(R.id.img_indicator);
                Glide.with(ImagePreviewActivity.this).load(itemData.getFilePath()).into(imageView);
                imageView.setOnClickListener(v -> {
                    if (!mediaFileInfos.contains(itemData)) return;
                    vpPhoto.setCurrentItem(mediaFileInfos.indexOf(itemData));
                    imageView.setBackgroundColor(getAppColor(R.color.color_07C160));
                });
                if (itemData.getFilePath() != currentFile.getFilePath()) {
                    imageView.setBackgroundColor(getAppColor(R.color.color_theme_background));
                } else {
                    imageView.setBackgroundColor(getAppColor(R.color.color_07C160));
                }
            }
        };
        recyclerIndicator.setAdapter(indicatorAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerIndicator.setLayoutManager(layoutManager);
    }

    private void initView() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) actionBar.getLayoutParams();
        layoutParams.topMargin = StatusBarUtil.getStatusBarHeight(this);
        actionBar.setLayoutParams(layoutParams);
        setStatusBarVisible(true);
        StatusBarUtil.setStatusBarColor(this, getAppColor(R.color.color_theme_background));
        StatusBarUtil.setStatusDarkEnable(this, false);
        NavigationBarUtil.setNavigationBarDarkStyle(getWindow(), false);
        NavigationBarUtil.setNavigationBarColor(getWindow(), getAppColor(R.color.color_theme_background));
        //底部按钮背景色透明度
        layoutBottomMenu.getBackground().setAlpha(245);
        ibBack.setOnClickListener(v -> onBackPressed());
        btnSend.setOnClickListener(v -> {
            if (checkedPathList.size() < 1) {
                checkedPathList.add(currentFile);
            }
            setResult(RESULT_OK);
            this.finish();
        });
        tbSelectOriginal.setChecked(isOriginal);
        tbSelectOriginal.setOnCheckedChangeListener((b, i) -> {
            isOriginal = i;
            if (i && checkedPathList.size() < 1) {
                checkedPathList.add(currentFile);
                indicatorList.add(currentFile);
                setCheckStatus(mediaFileInfos.indexOf(currentFile));
            }

        });
        setSendBtn();
    }

    private void setSendBtn() {
        int count = getCheckedCount();
        if (count > 0) {
            btnSend.setText("发送(" + count + "/" + maxSelectCount + ")");
        } else {
            btnSend.setText("发送");
        }
    }


    /**
     * 获取选中的数量
     *
     * @return
     */
    private int getCheckedCount() {
        return checkedPathList.size();
    }


    /**
     * 设置数据源
     */
    private void setSourceData() {
        position = getIntent().getIntExtra("position", -1);
        int flag = getIntent().getIntExtra("flag", -1);
        if (flag == PREVIEW_CHECKED) {
            this.checkedPathList = AlbumPreviewActivity.checkedPathList;
            this.mediaFileInfos = new ArrayList<>(AlbumPreviewActivity.checkedPathList);
        } else if (flag == PREVIEW_ALL) {
            this.checkedPathList = AlbumPreviewActivity.checkedPathList;
            this.mediaFileInfos = AlbumPreviewActivity.mediaFileInfoList;
        }
        this.maxSelectCount = AlbumPreviewActivity.maxSelectCount;
        indicatorList.addAll(new ArrayList<>(checkedPathList));
        if (mediaFileInfos.size() > 0) {
            currentFile = mediaFileInfos.get(position);
            tvTitle.setText(position + 1 + "/" + mediaFileInfos.size());
            if (checkedPathList.contains(mediaFileInfos.get(position))) {
                cbSelected.setChecked(true);
            } else {
                cbSelected.setChecked(false);
            }
        }
        showVideoOpen();
    }

    private void showVideoOpen() {
        if (currentFile.getMimeType().contains("video")) {
            ivVideoOpen.setVisibility(View.VISIBLE);
            ivVideoOpen.setOnClickListener(v -> startVideoView());
        } else {
            ivVideoOpen.setVisibility(View.GONE);
            ivVideoOpen.setOnClickListener(null);
        }
    }

    private void initImageAdapter() {
        photoViewAdapter = new PhotoViewAdapter(getSupportFragmentManager(), mediaFileInfos);
        vpPhoto.setAdapter(photoViewAdapter);
        vpPhoto.setOffscreenPageLimit(3);
        vpPhoto.setCurrentItem(position, true);
        photoViewAdapter.notifyDataSetChanged();
        vpPhoto.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCheckStatus(position);
                indicatorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        cbSelected.setOnClickListener(v -> {
            MediaFileInfo mediaFileInfo = mediaFileInfos.get(vpPhoto.getCurrentItem());
            int count = getCheckedCount();

            if (!cbSelected.isChecked()) {
                cbSelected.setChecked(false);
                checkedPathList.remove(mediaFileInfo);
                indicatorAdapter.notifyItemRemoved(indicatorList.indexOf(mediaFileInfo));
                indicatorList.remove(mediaFileInfo);
            } else {
                if (count >= maxSelectCount) {
                    cbSelected.setChecked(false);
                    showToast("你最多只能选择" + maxSelectCount + "个图片或视频");
                    return;
                }
                cbSelected.setChecked(true);
                checkedPathList.add(mediaFileInfo);
                indicatorList.add(indicatorList.size(), mediaFileInfo);
                indicatorAdapter.notifyItemInserted(indicatorList.indexOf(mediaFileInfo));
            }
            recyclerIndicator.scrollToPosition(indicatorList.indexOf(mediaFileInfo));
            setSendBtn();
        });
        vpPhoto.setClickable(true);
        vpPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideActionBar();
            }
        });

    }

    /**
     * 设置数字的变化
     *
     * @param position
     */
    private void setCheckStatus(int position) {
        currentFile = mediaFileInfos.get(position);
        recyclerIndicator.scrollToPosition(indicatorList.indexOf(currentFile));
        tvTitle.setText((position + 1) + "/" + mediaFileInfos.size());
        if (checkedPathList.size() > 0) {
            if (checkedPathList.contains(currentFile)) {
                cbSelected.setChecked(true);
            } else {
                cbSelected.setChecked(false);
            }
        }
        setSendBtn();
        showVideoOpen();
    }

    private void startVideoView() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri1 = BitmapUtils.filePathToUri(this, currentFile.getFilePath());
        Uri uri = MediaStore.Video.Media.getContentUri("external");
        intent.setDataAndType(Uri.withAppendedPath(uri, "" + currentFile.getFileId()),
                currentFile.getMimeType());
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class PhotoViewAdapter extends PagerAdapter {
        private List<MediaFileInfo> data;


        public PhotoViewAdapter(@NonNull FragmentManager fm, List<MediaFileInfo> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(ImagePreviewActivity.this);
            if (data.get(position).getMimeType().contains("video")) {
                photoView.disenable();
            } else {
                photoView.enable();
            }
            photoView.setMaxScale(4f);
            photoView.setInterpolator(new LinearInterpolator());
            photoView.setAnimaDuring(200);
            Glide.with(ImagePreviewActivity.this)
                    .load(data.get(position).getFilePath())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(photoView);
            container.addView(photoView);
            return photoView;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void showHideActionBar() {
        AlphaAnimation alphaAnimation = null;
        ValueAnimator translationYAnimator = null;
        if (actionBar.getVisibility() == View.VISIBLE) {
            alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    layoutBottomMenu.setVisibility(View.GONE);

                }
            });
            translationYAnimator = ValueAnimator.ofFloat(0, -actionBar.getHeight());
            translationYAnimator.setInterpolator(new AccelerateInterpolator());
            translationYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    actionBar.setTranslationY((Float) animation.getAnimatedValue());
                    if ((Float) animation.getAnimatedValue() == -actionBar.getHeight()) {
                        actionBar.setVisibility(View.GONE);
                        setStatusBarVisible(false);
                    }
                }
            });

        } else {
            alphaAnimation = new AlphaAnimation(0, 1);
            setStatusBarVisible(true);
            StatusBarUtil.setStatusDarkEnable(ImagePreviewActivity.this, false);
            layoutBottomMenu.setVisibility(View.VISIBLE);
            actionBar.setVisibility(View.VISIBLE);
            alphaAnimation.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
            });
            translationYAnimator = ValueAnimator.ofFloat(actionBar.getTranslationY(), 0);
            translationYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    actionBar.setTranslationY((Float) animation.getAnimatedValue());
                }
            });
        }
        alphaAnimation.setDuration(400);
        translationYAnimator.setDuration(300);
        layoutBottomMenu.startAnimation(alphaAnimation);
        translationYAnimator.start();
    }

    /**
     * 显示和隐藏状态栏
     *
     * @param show
     */
    private void setStatusBarVisible(boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (show) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
    }

    public void showToast(String msg) {
        ToastUtils.ShowToast(this, msg);
    }

    public int getAppColor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return this.getColor(id);
        } else {
            return ContextCompat.getColor(this, id);
        }
    }
}
