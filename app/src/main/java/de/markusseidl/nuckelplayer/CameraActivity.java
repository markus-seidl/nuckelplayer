package de.markusseidl.nuckelplayer;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private PreviewView previewView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);

        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.get(Manifest.permission.CAMERA)) {
                startCamera();
            } else {
                Toast.makeText(this, "Not enough camera permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).launch(new String[]{Manifest.permission.CAMERA});

    }

    protected void startCamera() {
        System.out.println("startCamera");

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                var cameraProvider = cameraProviderFuture.get();
                var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                var preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (ExecutionException e) {
                finish();
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                finish();
                throw new RuntimeException(e);
            }

        }, ContextCompat.getMainExecutor(this));
    }

}
