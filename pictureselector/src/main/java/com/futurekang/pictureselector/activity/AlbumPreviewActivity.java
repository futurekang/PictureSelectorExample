package com.futurekang.pictureselector.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.futurekang.pictureselector.R;
import com.futurekang.pictureselector.adapter.RCommAdapter;
import com.futurekang.pictureselector.model.FolderInfo;
import com.futurekang.pictureselector.model.MediaFileInfo;
import com.futurekang.pictureselector.tools.MediaScannerTools;
import com.futurekang.pictureselector.tools.MediaStoreDataManager;
import com.futurekang.pictureselector.tools.NavigationBarUtil;
import com.futurekang.pictureselector.tools.StatusBarUtil;
import com.futurekang.pictureselector.tools.TakePhotoTools;
import com.futurekang.pictureselector.tools.TimeUtils;
import com.futurekang.pictureselector.tools.ToastUtils;
import com.futurekang.pictureselector.tools.Utils;

import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class AlbumPreviewActivity extends AppCompatActivity {
    RecyclerView recyclerFolder;
    View ivForeground;
    ImageView testArrow;
    LinearLayout layoutTopMenu;
    TextView tvFolderName;
    FrameLayout layoutFolderContainer;
    private String TAG = "AlbumPreviewTag";

    TextView tvImgTime;
    CheckBox tbSelectOriginal;
    Button btnPreview;
    ImageButton ibBack;
    RecyclerView recyclerPhotoList;
    Button btnSend;
    RelativeLayout layoutBottomMenu;
    RelativeLayout actionBar;

    private RCommAdapter<MediaFileInfo> photoAdapter;
    private RCommAdapter<FolderInfo> folderAdapter;

    private List<FolderInfo> folderInfoList = new ArrayList<>();
    protected static List<MediaFileInfo> mediaFileInfoList = new ArrayList<>();
    protected static HashSet<MediaFileInfo> checkedPathList = new HashSet<>();
    protected static boolean isOriginal = false;

    protected static int maxSelectCount = 1;

    private FolderInfo currentFolder;

    private MediaStoreDataManager storeDataManager;

    private String allFolderName = "图片和视频";

    private String CAMERA_FILE_PATH;

    public static final String SELECT_COUNT = "select_count";

    /**
     * 入口
     *
     * @param activity
     * @param count
     */
    public static void toAlbumPreview(Activity activity, int count) {
        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.SELECT_COUNT, count);
        activity.startActivity(intent);
    }

    /**
     * 返回图片信息
     */
    private void setResult() {
        if (checkedPathList.size() < 1) {
            setResult(RESULT_CANCELED);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("isOriginal", tbSelectOriginal.isChecked());
        intent.putParcelableArrayListExtra("media_info", new ArrayList<>(checkedPathList));
        setResult(RESULT_OK, intent);
        this.finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setAppCompatWindowNoTitle(this);
        setContentView(R.layout.activity_album_preview);
        maxSelectCount = getIntent().getIntExtra(SELECT_COUNT, 1);
        initWindow();//初始化窗口
        initView();
        if (checkStoragePermission()) {//检查权限
            initAlbum();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (photoAdapter != null) {
            photoAdapter.notifyDataSetChanged();
            tbSelectOriginal.setChecked(isOriginal);
        }
    }

    private void initAlbum() {
        storeDataManager = new MediaStoreDataManager(this);

        initPhotoAdapter();
        initFolderAdapter();
        loadFolderData();
    }

    private void initView() {
        recyclerPhotoList = findViewById(R.id.recycler_photo_list);
        ivForeground = findViewById(R.id.iv_foreground);
        layoutFolderContainer = findViewById(R.id.layout_folder_container);
        recyclerFolder = findViewById(R.id.recycler_folder);
        tvImgTime = findViewById(R.id.tv_img_time);
        actionBar = findViewById(R.id.action_bar);
        ibBack = findViewById(R.id.ib_back);
        layoutTopMenu = findViewById(R.id.layout_top_menu);
        tvFolderName = findViewById(R.id.tv_folder_name);
        testArrow = findViewById(R.id.test_arrow);
        btnSend = findViewById(R.id.btn_send);
        layoutBottomMenu = findViewById(R.id.layout_bottom_menu);
        tbSelectOriginal = findViewById(R.id.tb_select_original);
        btnPreview = findViewById(R.id.btn_preview);

        setButtonNum();//设置底部按钮

        layoutTopMenu = findViewById(R.id.layout_top_menu);
        layoutTopMenu.setOnClickListener(v -> {
            if (recyclerFolder.getVisibility() == View.VISIBLE)
                hideFolderList();
            else
                showFolderList();
        });
        ivForeground.setOnClickListener(v -> {
            if (recyclerFolder.getVisibility() == View.VISIBLE)
                hideFolderList();
            else
                showFolderList();
        });
        ibBack.setOnClickListener(v -> onBackPressed());

        btnSend.setOnClickListener(v -> {
            setResult();
        });
        tbSelectOriginal.setOnCheckedChangeListener((b, i) -> isOriginal = i);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) layoutFolderContainer.getLayoutParams();
        layoutParams.height = Utils.getScreenHeight(this) * (2 / 3);
        layoutFolderContainer.setLayoutParams(layoutParams);
    }


    private void initFolderAdapter() {
        folderAdapter = new RCommAdapter<FolderInfo>(folderInfoList, R.layout.widget_folder_item) {
            @Override
            public void setView(RCViewHolder viewHolder, int position, FolderInfo folderFile) {
                setFolderInfo(viewHolder, folderFile);
            }
        };
        recyclerFolder.setAdapter(folderAdapter);
        recyclerFolder.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 显示文件夹列表
     */
    private void showFolderList() {
        if (rotateValueAnimator != null && rotateValueAnimator.isStarted()) return;
        rotateDrawable();
        ivForeground.setVisibility(View.VISIBLE);
        recyclerFolder.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(-recyclerFolder.getHeight(), 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                recyclerFolder.setTranslationY((Float) animation.getAnimatedValue());
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 0.9f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        ivForeground.startAnimation(alphaAnimation);
        ivForeground.setClickable(true);
    }


    /**
     * 隐藏文件夹列表
     */
    private void hideFolderList() {
        if (rotateValueAnimator != null && rotateValueAnimator.isStarted()) return;
        rotateDrawable();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, -recyclerFolder.getHeight());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                recyclerFolder.setTranslationY(value);
                if (value == -recyclerFolder.getHeight()) {
                    recyclerFolder.setVisibility(View.GONE);
                }
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        ivForeground.startAnimation(alphaAnimation);
        ivForeground.setClickable(false);
    }


    private void initPhotoAdapter() {
        int screenWidth = Utils.getScreenWidth(this);
        int marginRight = Utils.dip2px(this, 2);
        int itemWidht = (screenWidth - marginRight * 4) / 4;
        int itemHeight = itemWidht;
        /**
         * 照片适配器
         */
        photoAdapter = new RCommAdapter<MediaFileInfo>(mediaFileInfoList, R.layout.widget_photoloader_item) {
            @Override
            public void setView(RCViewHolder viewHolder, int position, MediaFileInfo itemData) {
                if (getItemViewType(position) == TYPE_NORMAL) {
                    setPhotoView(viewHolder, position, itemData, itemWidht, itemHeight);
                } else {
                    if (getItemViewType(position) == TYPE_HEADER) {
                        viewHolder.itemView.setOnClickListener(v -> {
                            if (checkCameraPermission()) {
                                CAMERA_FILE_PATH = TakePhotoTools.takePhoto(AlbumPreviewActivity.this, CAMERA_REQUESTCODE, getString(R.string.sdcard_path));
                            }
                        });
                    }
                }
            }
        };
        photoAdapter.setHeaderViewId(R.layout.widget_photoloader_hearder);
        recyclerPhotoList.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                Glide.get(AlbumPreviewActivity.this).clearMemory();
            }
        });

        recyclerPhotoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (mediaFileInfoList.size() < 1) return;
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                int position = manager.findFirstVisibleItemPosition();
                showTime(mediaFileInfoList.get(position).getTime());
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    hideDateTime(500);
                }

            }
        });
        hideDateTime(0);
        recyclerPhotoList.setItemViewCacheSize(24);
        recyclerPhotoList.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerPhotoList.setAdapter(photoAdapter);
    }

    /**
     * 设置每一张预览图的属性
     *
     * @param viewHolder
     * @param itemData
     * @param itemWidht
     * @param itemHeight
     */
    private void setPhotoView(RCommAdapter.RCViewHolder viewHolder, int position, MediaFileInfo
            itemData, int itemWidht, int itemHeight) {
        ImageView imageView = viewHolder.getItemView(R.id.iv_thumbnail);
        TextView tvDuration = viewHolder.getItemView(R.id.tv_duration);
        //视频
        if (itemData.getMimeType().contains("video")) {
            tvDuration.setVisibility(View.VISIBLE);
            tvDuration.setText(TimeUtils.TimestampToDate(itemData.getDuration(), "mm:ss"));
        } else {
            tvDuration.setVisibility(View.GONE);
        }
        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(itemWidht, itemHeight));
        Glide.with(viewHolder.itemView)
                .load(itemData.getFilePath())
                .centerCrop()
                .thumbnail(0.9f)
                .into(imageView);
        imageView.setOnClickListener(v -> toPreviewActivity(itemData, position));
        CheckBox checkBox = viewHolder.getItemView(R.id.cb_chebox);
        boolean isChecked = checkedPathList.contains(itemData);
        checkBox.setChecked(isChecked);
        checkBox.setOnClickListener(v -> {
            if (!checkBox.isChecked()) {
                checkBox.setChecked(false);
                checkedPathList.remove(itemData);
            } else {
                if (getCheckedCount() >= maxSelectCount) {
                    checkBox.setChecked(false);
                    showToast("你最多只能选择" + maxSelectCount + "个图片或视频");
                    return;
                }
                checkBox.setChecked(true);
                checkedPathList.add(itemData);
            }
            setButtonNum();
        });
    }

    private void toPreviewActivity(MediaFileInfo itemData, int position) {
        if (photoAdapter.getHeaderViewId() != -1) {
            position = position - 1;
        }
        ImagePreviewActivity.startImagePreviewAll(this, position);
    }

    /**
     * 获取选中的数量
     *
     * @return
     */
    private int getCheckedCount() {
        return checkedPathList.size();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setButtonNum() {
        int count = getCheckedCount();
        if (count == 0) {
            btnPreview.setText("预览");
            btnPreview.setTextColor(getAppColor(R.color.color_8a8a8a));
            btnSend.setText("发送");
            btnSend.setVisibility(View.GONE);
            btnPreview.setClickable(false);
            btnPreview.setOnClickListener(null);
        } else {
            btnSend.setVisibility(View.VISIBLE);
            btnPreview.setText("预览(" + count + ")");
            btnSend.setText("发送(" + count + "/" + maxSelectCount + ")");
            btnPreview.setTextColor(getAppColor(R.color.color_theme_text_color));
            btnPreview.setClickable(true);
            btnPreview.setOnClickListener(v -> {
                ImagePreviewActivity.startImagePreviewChecked(this, 0);
            });
        }
    }


    public int getAppColor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return this.getColor(id);
        } else {
            return ContextCompat.getColor(this, id);
        }
    }

    /**
     * 设置文件夹属性
     *
     * @param viewHolder
     * @param folderFile
     */
    private void setFolderInfo(RCommAdapter.RCViewHolder viewHolder, FolderInfo folderFile) {
        ImageView tiltePage = viewHolder.getItemView(R.id.iv_title_page);
        TextView folderName = viewHolder.getItemView(R.id.tv_folder_name);
        TextView fileCount = viewHolder.getItemView(R.id.tv_file_count);
        RadioButton radioButton = viewHolder.getItemView(R.id.rb_radio);
        radioButton.setClickable(false);
        if (folderFile == currentFolder) {
            radioButton.setChecked(true);
        } else {
            radioButton.setChecked(false);
        }
        folderName.setText(folderFile.getFolderName());
        fileCount.setText("(" + folderFile.getChildFileInfo().size() + ")");
        Glide.with(this)
                .load(folderFile.getChildFileInfo().get(0).getFilePath())
                .centerCrop()
                .thumbnail()
                .into(tiltePage);
        viewHolder.itemView.setOnClickListener(v -> {
            hideFolderList();
            if (radioButton.isChecked()) {
                return;
            }
            //清理之前显示的
            this.mediaFileInfoList.clear();
            this.mediaFileInfoList.addAll(folderFile.getChildFileInfo());
            folderAdapter.notifyItemChanged(folderInfoList.indexOf(currentFolder));
            currentFolder = folderFile;
            tvFolderName.setText(folderFile.getFolderName());
            photoAdapter.notifyDataSetChanged();
        });
    }


    /**
     * 加载文件夹的缩略图列表
     */
    private void loadFolderData() {
        /**
         * 第一步获取到所有的媒体文件信息
         */
        Observable observable = Observable.just(storeDataManager.getImageAndVideo());
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        observable.subscribeWith(new DisposableObserver<List<MediaFileInfo>>() {
            @Override
            public void onNext(List<MediaFileInfo> o) {
                mediaFileInfoList.clear();
                folderInfoList.clear();
                mediaFileInfoList.addAll(o);
                photoAdapter.notifyDataSetChanged();
                folderInfoList.addAll(getFolderInfos());
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });


    }

    @NotNull
    private List<FolderInfo> getFolderInfos() {
        List<FolderInfo> folderInfoList = new ArrayList<>();
        /**
         * 第二步，根据文件信息找出对应的文件夹
         */
        Set<String> folderPathList = new HashSet<>();
        for (MediaFileInfo mediaFileInfo : mediaFileInfoList) {
            folderPathList.add(mediaFileInfo.getParentPath());
            Log.d(TAG, "getFolderInfos: " + mediaFileInfo.getMimeType());
        }
        for (String folderPath : folderPathList) {
            ArrayList<MediaFileInfo> tempList = getChildList(folderPath);
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setFolderName(folderPath.substring(folderPath.lastIndexOf("/") + 1, folderPath.length()));
            folderInfo.setFileCount(tempList.size());
            folderInfo.setChildFileInfo(tempList);
            folderInfo.setFolderPath(folderPath);
            folderInfo.setFolderType(0);
            folderInfoList.add(folderInfo);
        }
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.setFolderName(allFolderName);
        folderInfo.setFileCount(mediaFileInfoList.size());
        folderInfo.setChildFileInfo(new ArrayList<>(mediaFileInfoList));
        folderInfo.setFolderType(0);
        folderInfo.setFolderPath("all");

        currentFolder = folderInfo;

        folderInfoList.add(folderInfo);
        Collections.sort(folderInfoList, (o1, o2) -> {
            Collator collator = Collator.getInstance(Locale.CHINA);
            return collator.compare(o1.getFolderName(), o2.getFolderName());
        });
        //中文排序
//        folderInfoList.sort();

        return folderInfoList;
    }


    private ArrayList<MediaFileInfo> getChildList(String parentPath) {
        ArrayList<MediaFileInfo> mediaTemp = new ArrayList<>();
        for (int i = 0; i < mediaFileInfoList.size(); i++) {
            if (parentPath.concat("/" + mediaFileInfoList.get(i).getFileName())
                    .equals(mediaFileInfoList.get(i).getFilePath())) {
                mediaTemp.add(this.mediaFileInfoList.get(i));
            }
        }
        return mediaTemp;
    }


    public void showToast(String msg) {
        ToastUtils.ShowToast(this, msg);
    }

    public void startActivityByAnimation(Class<?> activity, Bundle bundle, View... views) {
        Pair<View, String>[] sharedElements = new Pair[views.length];
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            Objects.requireNonNull(view.getTransitionName());
            Pair<View, String> pView = Pair.create(view, view.getTransitionName());
            sharedElements[i] = pView;
        }
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedElements);
        Intent intent = new Intent(this, activity);
        intent.putExtras(bundle);
        ActivityCompat.startActivity(this, intent, compat.toBundle());
    }

    private void showTime(long date) {
        String time = TimeUtils.TimestampToDate(date, "yyyy-MM");
        if (TimeUtils.isThisMonth(date)) {
            time = "本月";
            if (TimeUtils.isThisWeek(date)) {
                time = "本周";
                if (TimeUtils.isToday(date)) {
                    time = "今天";
                }
            }
        }
        tvImgTime.getBackground().setAlpha(255);
        tvImgTime.setText(time);

    }

    private void hideDateTime(int duration) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        tvImgTime.startAnimation(alphaAnimation);
    }

    private ValueAnimator rotateValueAnimator;

    private void rotateDrawable() {
        if (rotateValueAnimator == null) {
            testArrow.setPivotX(testArrow.getWidth() / 2);
            testArrow.setPivotY(testArrow.getHeight() / 2);//支点在图片中心
            float currentRotation = testArrow.getRotation();
            rotateValueAnimator = ValueAnimator.ofInt();
            rotateValueAnimator.setDuration(300).addUpdateListener(animation -> {
                int angle = (int) animation.getAnimatedValue();
                testArrow.setRotation(angle);
            });
        }
        rotateValueAnimator.setIntValues((int) testArrow.getRotation(), (int) (testArrow.getRotation() + 180));
        if (!rotateValueAnimator.isRunning()) {
            rotateValueAnimator.start();
        }
    }

    private void initWindow() {
        StatusBarUtil.setStatusBarColor(this, getAppColor(R.color.color_theme_background));
        StatusBarUtil.setStatusDarkEnable(this, false);
        NavigationBarUtil.setNavigationBarDarkStyle(getWindow(), false);
        NavigationBarUtil.setNavigationBarColor(getWindow(), getAppColor(R.color.color_theme_background));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaFileInfoList.clear();
        checkedPathList.clear();
        Glide.get(AlbumPreviewActivity.this).clearMemory();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private int STORAGE_REQUESTCODE = 1000;
    private int CAMERA_REQUESTCODE = 1001;

    /**
     * 检测存储权限
     */
    public boolean checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUESTCODE);
            return false;
        }
        return true;
    }

    /**
     * 检测相机权限
     */
    public boolean checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUESTCODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
        if (grantResults.length > 0) {
        } else {
            return;
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            ToastUtils.ShowToast(this, "未获取到权限，功能无法正常使用");
            return;
        }
        if (requestCode == STORAGE_REQUESTCODE) {
            initAlbum();
        } else if (requestCode == CAMERA_REQUESTCODE) {
            //调用相机拍照
            CAMERA_FILE_PATH = TakePhotoTools.takePhoto(this, CAMERA_REQUESTCODE, getString(R.string.sdcard_path));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10000) {
                setResult();
            } else if (requestCode == CAMERA_REQUESTCODE) {
                MediaScannerTools.scan(this, CAMERA_FILE_PATH)
                        .listener(new MediaScannerTools.Listener() {
                            @Override
                            public void onResult(String path, Uri uri) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadFolderData();
                                    }
                                });
                                Log.d(TAG, "onResult: " + (uri == null ? "失败" : "成功") + "  path = " + path);
                            }
                        });

            }
        }
    }
}
