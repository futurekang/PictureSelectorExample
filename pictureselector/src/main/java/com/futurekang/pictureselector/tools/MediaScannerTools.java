package com.futurekang.pictureselector.tools;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MediaScannerTools implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mPath;

    private MediaScannerTools(Context context, String path) {
        mPath = path;
        mConnection = new MediaScannerConnection(context, this);
        mConnection.connect();
    }

    public static MediaScannerTools scan(Context context, String path) {
        return new MediaScannerTools(context, path);
    }

    public void onMediaScannerConnected() {
        mConnection.scanFile(mPath, null);
    }

    public void onScanCompleted(String path, Uri uri) {
        Log.d(TAG, "onScanCompleted: " + path);
        if (uri != null) {
            Log.d(TAG, "onScanCompleted: 扫描成功" + uri.getPath());
        } else {
            Log.d(TAG, "onScanCompleted: 扫描失败");
        }
        if (listener != null) {
            listener.onResult(path, uri);
        }
        mConnection.disconnect();
    }

    Listener listener;

    public interface Listener {
        void onResult(String path, Uri uri);
    }

    public void listener(Listener listener) {
        this.listener = listener;
    }
}
