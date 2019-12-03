package com.futurekang.pictureselector.tools;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.os.EnvironmentCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TakePhotoTools {

    /**
     * @param context
     * @param requestCode
     * @param parentDir   设置父目录,必须设置
     * @return
     */
    public static String takePhoto(Context context, int requestCode, String parentDir) {
        if (parentDir == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        //获取SD卡根目录，（手机当前默认存储位置）如果这里设置应用私有目录，图库无法扫描到图片
        File rootDir = Environment.getExternalStorageDirectory();
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }
        //获取文件夹父目录，自己指定
        File imgStorageDir = new File(rootDir, parentDir);
        if (!imgStorageDir.exists()) {
            imgStorageDir.mkdir();
        }
        //时间戳生成图片名称
        String imageFileName = String.format("JPEG_%s.jpg", dateFormat.format(new Date()));
        File imgFile = new File(imgStorageDir, imageFileName);
        //判断是否有权限读取
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(imgFile))) {
            return null;
        }
        // 直接向媒体库插入数据
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, imgFile.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        //构建隐式意图，调用系统相机
        Intent intent = new Intent();
        //表示给相机组件发送
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //这里我们指定了输出的uri那么我们在onActivityResult时不能直接使用intent获取数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        ((Activity) context).startActivityForResult(intent, requestCode);
        //返回文件地址
        return imgFile.getAbsolutePath();
    }
}
