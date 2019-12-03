package com.futurekang.pictureselector.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

/**
 * 沉浸式窗口工具类
 * Activity中调用
 */
public class StatusBarUtil {

    /**
     * @param color  状态栏的颜色
     * @param enable 是否开启状态栏沉浸(这里其实隐藏了actionBar，但是状态栏还在,开启前确认布局顶部是自己要的颜色)
     * @param drak   切换图标颜色（不能同FLAG_TRANSLUCENT_STATUS（setStatusBarTransparent） 同时为true，会失效）
     */
    public static void setStatusBarStyle(Activity mActivity, @ColorInt int color, boolean enable, boolean drak) {
        clearFlag(mActivity);
        //注三个方法调用顺序不能更改
        setStatusBarColor(mActivity, color);
        setStatusDarkEnable(mActivity, drak);
        setTransparentEnable(mActivity, enable);
    }

    /**
     * 清理之前设置的Flag
     *
     * @param activity
     */
    private static void clearFlag(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(0);
    }

    /**
     * 沉浸式效果
     *
     * @param mActivity
     * @param enable
     */
    public static void setTransparentEnable(Activity mActivity, boolean enable) {
        Window window = mActivity.getWindow();
        View decorView = mActivity.getWindow().getDecorView();
        int originFlag = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        if (enable) {
            originFlag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        } else {
            originFlag &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
        decorView.setSystemUiVisibility(originFlag);
    }

    public static void setSystemUIFullscreen(Activity mActivity, boolean enable) {
        Window window = mActivity.getWindow();
        if (enable) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    ;

    /**
     * 设置状态栏色调风格  亮色/暗色
     *
     * @param mActivity
     * @param isDark    暗色
     */
    public static void setStatusDarkEnable(Activity mActivity, boolean isDark) {
        Window window = mActivity.getWindow();
        // 设置浅色状态栏时的界面显示
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isDark) {
                ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        decor.setSystemUiVisibility(ui);
    }


    /**
     * 设置状态栏背景色透明
     */
    public static void setStatusBarTransparent(Activity mActivity) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = mActivity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //也可以设置成灰色透明的，比较符合Material Design的风格
            mActivity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }


    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param colorId
     */
    public static void setStatusBarColor(Activity mActivity, @ColorInt int colorId) {
        Window window = mActivity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(colorId);
        }
    }

    /**
     * Return the height of the status bar
     *
     * @param context The context
     * @return Height of the status bar
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static void setWindowNoTitle(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public static void setAppCompatWindowNoTitle(AppCompatActivity activity) {
        activity.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}
