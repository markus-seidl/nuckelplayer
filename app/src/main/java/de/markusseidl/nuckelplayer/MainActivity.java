package de.markusseidl.nuckelplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton btnVideoPlayer;

    ImageButton btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnVideoPlayer = findViewById(R.id.btnMainVideos);
        btnVideoPlayer.setOnClickListener(v -> {
            var intent = new Intent(MainActivity.this, VideoOverview.class);
            startActivity(intent);
        });

        btnCamera = findViewById(R.id.btnMainCamera);
        btnCamera.setOnClickListener(v -> {
            var intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });
    }

}