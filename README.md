# PictureSelectorExample
仿微信的一个图片选择器。


 1.
 
you **module** AndroidManifest

```java
 <activity android:name="com.futurekang.pictureselector.activity.AlbumPreviewActivity" />
 <activity android:name="com.futurekang.pictureselector.activity.ImagePreviewActivity" />
```

 2. 

you **app/build.gradle**

```java
    dependencies  {
    	implementation project(path: ':pictureselector')
    }
```

 3. 

```java
  Button button = findViewById(R.id.btn_go);
        button.setOnClickListener(v ->
                AlbumPreviewActivity.toAlbumPreview(MainActivity.this, 9)
        );
```






![Image text](https://github.com/futurekang/Picture-Folder/blob/master/20191203104948.jpg)

![Image text](https://github.com/futurekang/Picture-Folder/blob/master/20191203105135.jpg)

![Image text](https://github.com/futurekang/Picture-Folder/blob/master/20191203105143.jpg)

![Image text](https://github.com/futurekang/Picture-Folder/blob/master/20191203105151.jpg)

![Image text](https://github.com/futurekang/Picture-Folder/blob/master/20191203105202.jpg)
