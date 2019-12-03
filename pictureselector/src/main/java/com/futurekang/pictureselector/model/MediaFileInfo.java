package com.futurekang.pictureselector.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaFileInfo implements Parcelable {

    private String fileId;
    private String fileName;
    private String filePath;
    private String parentPath;
    private long duration;
    private String mimeType;
    private int width;
    private int height;
    private long time;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileId);
        dest.writeString(this.fileName);
        dest.writeString(this.filePath);
        dest.writeString(this.parentPath);
        dest.writeLong(this.duration);
        dest.writeString(this.mimeType);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.time);
    }

    public MediaFileInfo() {
    }

    protected MediaFileInfo(Parcel in) {
        this.fileId = in.readString();
        this.fileName = in.readString();
        this.filePath = in.readString();
        this.parentPath = in.readString();
        this.duration = in.readLong();
        this.mimeType = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.time = in.readLong();
    }

    public static final Creator<MediaFileInfo> CREATOR = new Creator<MediaFileInfo>() {
        @Override
        public MediaFileInfo createFromParcel(Parcel source) {
            return new MediaFileInfo(source);
        }

        @Override
        public MediaFileInfo[] newArray(int size) {
            return new MediaFileInfo[size];
        }
    };
}
