package com.futurekang.pictureselector.tools;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.futurekang.pictureselector.model.MediaFileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreDataManager {

    private Context mContext;

    public MediaStoreDataManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 获取在媒体库中的图像记录
     *
     * @return 图片信息集合或者视屏信息集合
     */
    public List<MediaFileInfo> getImageAndVideo() {
        List<MediaFileInfo> mediaFileInfos = new ArrayList<>();
        //asc 按升序排列
        // desc 按降序排列
        //projection 是定义返回的数据，selection 通常的sql 语句，例如
        // selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? " 那么 selectionArgs=new String[]{"jpg"};
        Cursor imgCursor, videoCursor;

        String[] imgProjection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.WIDTH,
                MediaStore.Images.ImageColumns.HEIGHT,
                MediaStore.Images.ImageColumns.DATE_MODIFIED

        };
        String[] videoProjection = new String[]{
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.MIME_TYPE,
                MediaStore.Video.VideoColumns.WIDTH,
                MediaStore.Video.VideoColumns.HEIGHT,
                MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Video.VideoColumns.DURATION
        };

        imgCursor = mContext.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imgProjection, null, null,
                        MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc");

        videoCursor = mContext.getContentResolver()
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        videoProjection, null, null,
                        MediaStore.Video.VideoColumns.DATE_MODIFIED + "  desc");

        String fileId;
        String fileName;
        String filePath;
        String mimeType;
        int width;
        int height;
        long duration;
        long time;
        File file;
        while (imgCursor.moveToNext()) {
            fileId = imgCursor.getString(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
            filePath = imgCursor.getString(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            fileName = imgCursor.getString(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
            mimeType = imgCursor.getString(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
            width = imgCursor.getInt(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH));
            height = imgCursor.getInt(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT));
            time = imgCursor.getLong(imgCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));
            file = new File(filePath);
            MediaFileInfo fileItem = new MediaFileInfo();
            fileItem.setFileId(fileId);
            fileItem.setFileName(fileName);
            fileItem.setFilePath(filePath);
            fileItem.setParentPath(file.getParentFile().getAbsolutePath());
            fileItem.setMimeType(mimeType);
            fileItem.setHeight(height);
            fileItem.setWidth(width);
            fileItem.setDuration(0);
            fileItem.setTime(time);
            mediaFileInfos.add(fileItem);
        }
        imgCursor.close();
        imgCursor = null;
        while (videoCursor.moveToNext()) {
            fileId = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
            filePath = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
            fileName = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
            mimeType = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
            width = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.WIDTH));
            height = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.HEIGHT));
            time = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED));
            duration = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
            file = new File(filePath);
            MediaFileInfo fileItem = new MediaFileInfo();
            fileItem.setFileId(fileId);
            fileItem.setFileName(fileName);
            fileItem.setFilePath(filePath);
            fileItem.setParentPath(file.getParentFile().getAbsolutePath());
            fileItem.setMimeType(mimeType);
            fileItem.setHeight(height);
            fileItem.setWidth(width);
            fileItem.setDuration(duration);
            fileItem.setTime(time);
            mediaFileInfos.add(fileItem);
        }
        videoCursor.close();
        videoCursor = null;
        return mediaFileInfos;
    }
}
