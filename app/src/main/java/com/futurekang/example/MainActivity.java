package com.futurekang.example;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.futurekang.pictureselector.activity.AlbumPreviewActivity;
import com.futurekang.pictureselector.model.MediaFileInfo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.btn_go);
        button.setOnClickListener(v ->
                AlbumPreviewActivity.toAlbumPreview(MainActivity.this, 9)
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            assert data != null;
            ArrayList<MediaFileInfo> mediaFileInfos = data.getParcelableArrayListExtra("media_info");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaFileInfos.forEach(mediaFileInfo -> Log.d(TAG, "accept: " + mediaFileInfo.getFilePath()));
            }
        }
    }

}
