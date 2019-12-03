package com.futurekang.pictureselector.model;

import java.util.ArrayList;

public class FolderInfo {

    private String folderName;
    private String folderPath;
    private int folderType;
    private int fileCount;
    private ArrayList<MediaFileInfo> childFileInfo;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public int getFolderType() {
        return folderType;
    }

    public void setFolderType(int folderType) {
        this.folderType = folderType;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public ArrayList<MediaFileInfo> getChildFileInfo() {
        return childFileInfo;
    }

    public void setChildFileInfo(ArrayList<MediaFileInfo> childFileInfo) {
        this.childFileInfo = childFileInfo;
    }

    public enum FolderType {
        VIDEO, IMAGE;
    }

    public final static int TYPE_VIDEO = 10000001;
    public final static int TYPE_IMAGE = 10000002;
}
