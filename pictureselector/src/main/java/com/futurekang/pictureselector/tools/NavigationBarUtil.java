package com.futurekang.pictureselector.tools;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

public class NavigationBarUtil {

    public static void setNavigationBarColor(Window window, @ColorInt int color) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setNavigationBarColor(color);
    }

    public static void setNavigationBarDarkStyle(Window window, boolean isDark) {
        // 设置浅色状态栏时的界面显示
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (isDark) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(ui);
    }

}
