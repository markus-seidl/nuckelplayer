package de.markusseidl.nuckelplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements VideoRVItem.VideoClickInterface {
    private RecyclerView videoRV;
    private VideoRVItem videoRVItem;

    private static final int STORAGE_PERMISSION = 101;

    private final Executor backgroundThreadExecutor = Executors.newFixedThreadPool(4);

    private GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoRV = findViewById(R.id.idRVVideos);
        videoRVItem = new VideoRVItem(this, this);

        gridLayoutManager = new GridLayoutManager(this, 4);
        videoRV.setLayoutManager(gridLayoutManager);
        videoRV.setAdapter(videoRVItem);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {
            getVideos();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager.setSpanCount(4);
        } else {
            gridLayoutManager.setSpanCount(2);
        }
    }

    @Override
    public void onVideoClick(int position) {
        Intent i = new Intent(this, VideoPlayerActivity.class);
        i.putExtra("videoIdx", position);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                getVideos(); // only for the first time
            } else {
                Toast.makeText(this, "Without this permission the app is useless - exiting.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void getVideos() {
        System.out.println("getVideos() called");
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        try (var cursor = contentResolver.query(uri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String videoTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)); // TODO
                    @SuppressLint("Range") String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)); // TODO

                    VideoRepository.getInstance().add(new VideoInformation(videoTitle, videoPath, null));
                    final int index = VideoRepository.getInstance().getVideoInformations().size() - 1;
                    backgroundThreadExecutor.execute(() -> {
                        Bitmap videoThumbnail = null;
                        try {
                            videoThumbnail = ThumbnailUtils.createVideoThumbnail(new File(videoPath), Size.parseSize("320x240"), null);
                            VideoRepository.getInstance().getVideoInformations().get(index).setThumbnail(videoThumbnail);

                            runOnUiThread(() -> {
                                videoRVItem.notifyItemChanged(index);
                            });
                        } catch (IOException e) {
                            System.out.println("Error with file " + videoPath);
                            e.printStackTrace();
                        }
                    });
                } while (cursor.moveToNext());
            }
            videoRVItem.notifyDataSetChanged();
        }
    }


}