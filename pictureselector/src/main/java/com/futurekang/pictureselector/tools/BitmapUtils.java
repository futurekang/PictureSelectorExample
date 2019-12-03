package com.futurekang.pictureselector.tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;

import androidx.core.content.FileProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    public static Bitmap getBitmapFormUri(Activity ac, Uri uri)
            throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;// optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        float hh = 800f;
        float ww = 480f;
        int be = 1;
        if (originalWidth > originalHeight && originalWidth > ww) {
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;
        bitmapOptions.inDither = true;// optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap, 100);
    }


    public static Bitmap compressImage(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        //循环判断如果压缩后图片是否大于maxMemmorrySize,大于继续压缩
        int currentLength = baos.toByteArray().length;
        while (currentLength / 1024 > size) {
            baos.reset(); //重置baos即让下一次的写入覆盖之前的内容
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
            currentLength = baos.toByteArray().length;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static File getFileFromMediaUri(Context ac, Uri uri) {
        if (uri.getScheme().toString().compareTo("content") == 0) {
            ContentResolver cr = ac.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor
                        .getColumnIndex("_data"));
                cursor.close();
                if (filePath != null) {
                    return new File(filePath);
                }
            }
        } else if (uri.getScheme().toString().compareTo("file") == 0) {
            return new File(uri.toString().replace("file://", ""));
        }
        return null;
    }


    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


    public static Bitmap returnPath(String filePath, int rqsW, int rqsH) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = caculateInSampleSize(options, rqsW, rqsH);
        int height = options.outHeight;
        int with = options.outWidth;
        System.out.println("Bitmap Height == " + options.outHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public final static int caculateInSampleSize(BitmapFactory.Options options,
                                                 int rqsW, int rqsH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0)
            return 1;
        if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > 1600 && height > 1600) {
            int tempWidth = width - 1600;
            int tempHeight = width - 1600;
            if (tempWidth > tempHeight) {
                height = 1600 * height / width;
                width = 1600;
            } else {
                width = 1600 * width / height;
                height = 1600;
            }
        } else if (width > 1600) {
            height = 1600 * height / width;
            width = 1600;
        } else if (height > 1600) {
            width = 1600 * width / height;
            height = 1600;
        }
        System.out.println("width:" + width + "height:" + height);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);

    }

    public static File generateBitmapFile() {

        String extDir = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "estronger" + File.separator + "bitmap" + File.separator;
        File file = new File(extDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmssSSS", Locale.CHINA);
        Date now = new Date();
        String filePath = extDir + format.format(now) + ".jpg";
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        return f;
    }

    /**
     * @return
     */
    public static File getBitmapFile(String filePath) {
        File f = BitmapUtils.generateBitmapFile();
        try {
            FileOutputStream out = new FileOutputStream(f);
            Bitmap bmp = BitmapUtils.returnPath(filePath, 1600, 1600);
            bmp = BitmapUtils.scaleBitmap(bmp);
            if (bmp != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }


    /**
     * @return
     */
    public static File getBitmapFile(Resources res, int resId) {
        File f = BitmapUtils.generateBitmapFile();
        try {
            FileOutputStream out = new FileOutputStream(f);
            Bitmap bmp = BitmapUtils.returnPath(res, resId, 1600, 1600);
            bmp = BitmapUtils.scaleBitmap(bmp);
            if (bmp != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public static Bitmap returnPath(Resources res, int resId, int rqsW, int rqsH) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = caculateInSampleSize(options, rqsW, rqsH);
        int height = options.outHeight;
        int with = options.outWidth;
        System.out.println("Bitmap Height == " + options.outHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 将base64加密串转换成Bitmap类型
     *
     * @param str
     * @return
     */
    public static Bitmap base64toBitmap(String str) { // 解码base64
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(str, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 图片转化成base64字符串
     */
    public static String bitmap2Base64(Bitmap bitmap) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] datas = baos.toByteArray();
            return Base64.encodeToString(datas, Base64.NO_WRAP); //返回Base64编码过的字节数组字符串
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }

    /**
     * 将图片切成圆形
     *
     * @param source
     * @return
     */
    public static Bitmap createCircleImage(Bitmap source) {
        int length = source.getWidth() < source.getHeight() ? source.getWidth() : source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(length / 2, length / 2, length / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    public static void saveBitmap(Bitmap bitmap) throws IOException {
        saveBitmap("/sdcard/DCIM/Camera/", bitmap);
    }

    /**
     * 保存图片
     *
     * @param bitmap
     * @throws IOException
     */
    public static File saveBitmap(String filePath, Bitmap bitmap) throws IOException {
        File file = null;
        file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
                return file;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean bitmapExists(String bitName) {
        return bitmapExists("/sdcard/DCIM/Camera/", bitName);
    }

    public static boolean bitmapExists(String filePath, String bitName) {
        File file = new File(filePath + bitName + ".png");
        return file.exists();
    }


    public static Bitmap getBitmap(String filePath) {
        try {
            return BitmapFactory.decodeFile(filePath, getBitmapOption(1)); //将图片的长和宽缩小味原来的1/2
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static BitmapFactory.Options getBitmapOption(int inSampleSize) {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    /**
     * file路径转uri
     *
     * @param path
     * @return
     */
    public static Uri filePathToUri(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri uri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            uri = FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".fileProvider", file);
        }
        return uri;
    }
}
