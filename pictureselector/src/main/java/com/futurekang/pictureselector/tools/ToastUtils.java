package com.futurekang.pictureselector.tools;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.futurekang.pictureselector.R;


public class ToastUtils {


    private static Toast toast;
    /**
     * 之前显示的内容
     */
    private static String oldMsg;
    /**
     * 第一次时间
     */
    private static long oneTime = 0;

    /**
     * @param context
     * @param msg     LENGTH_SHORT
     */
    public static void ShowToast(Context context, String msg) {
        ShowToast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void ShowToast(Context context, String msg, int Duration) {
        //获取自定义视图
        View view = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
        if (toast == null) {
            toast = new Toast(context);
            //设置Toast显示位置，居中，向 X、Y轴偏移量均为0
            toast.setGravity(Gravity.CENTER, 0, 0);
            //获取自定义视图
            ((TextView) view.findViewById(R.id.tv_message_toast)).setText(msg);
            //设置视图
            toast.setView(view);
            //设置显示时长
            toast.setDuration(Duration);
            oneTime = System.currentTimeMillis();
            toast.show();
        } else {
            long twoTime = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (twoTime - oneTime > Duration) {
                    toast.show();
                }
            } else {
                oldMsg = msg;
                ((TextView) view.findViewById(R.id.tv_message_toast)).setText(msg);
                toast.setView(view);
                toast.show();
            }
            oneTime = twoTime;
        }
    }
}
