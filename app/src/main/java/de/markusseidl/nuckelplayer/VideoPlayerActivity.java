package de.markusseidl.nuckelplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerActivity extends AppCompatActivity {

    private TextView videoNameTV;
    private ImageButton backIB;
    private ImageButton playPauseIB;
    private ImageButton forwardIB;

    private ImageButton prevIB;
    private ImageButton nextIB;
    private ImageButton overviewIB;

    private VideoView videoView;
    private RelativeLayout controlsRL, videoRL;
    boolean isOpen = true; // show custom controls?
    private int currentVideoIdx;

    private boolean allowPlayOnce = true;

    private Handler autoHideControlsHandler;
    private final Runnable autoHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideControls();
            autoHideControlsHandler.removeCallbacks(autoHideRunnable);
        }
    };

    private final Runnable blockNextRunnable = new Runnable() {
        @Override
        public void run() {
            enableButton(nextIB);
            autoHideControlsHandler.removeCallbacks(blockNextRunnable);
        }
    };

    private final Runnable blockPrevRunnable = new Runnable() {
        @Override
        public void run() {
            enableButton(prevIB);
            autoHideControlsHandler.removeCallbacks(blockPrevRunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        var startVideoIdx = getIntent().getIntExtra("videoIdx", 0);

        videoNameTV = findViewById(R.id.idTVVideoTitle);
        backIB = findViewById(R.id.idIBBack10);
        playPauseIB = findViewById(R.id.idIBPlay);
        forwardIB = findViewById(R.id.idIBForward10);
        prevIB = findViewById(R.id.idIBSkipPrev);
        nextIB = findViewById(R.id.idIBSkipNext);
        overviewIB = findViewById(R.id.idIBToOverview);

        videoView = findViewById(R.id.idVideoView);
        controlsRL = findViewById(R.id.idRLControls);
        videoRL = findViewById(R.id.idRLVideo);
        autoHideControlsHandler = new Handler(Looper.getMainLooper());

        videoView.setOnPreparedListener(mp -> {
            System.out.println("Video prepared");
            if(allowPlayOnce) {
                videoView.start();
                allowPlayOnce = false;
                playPauseIB.setImageResource(R.drawable.ic_pause);
            } else {
                showControls();
            }
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            System.out.println("Error: " + what + " " + extra);
            finish();
            return false;
        });

        videoView.setOnCompletionListener(mp -> {
            showControls();
        });

        playPauseIB.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseIB.setImageResource(R.drawable.ic_play);

            } else {
                videoView.start();
                playPauseIB.setImageResource(R.drawable.ic_pause);
            }
            postponeAutoHideControls();
        });
        playPauseIB.setImageResource(R.drawable.ic_pause);

        forwardIB.setOnClickListener(v -> {
            final int skipTime = 10_000;
            videoView.seekTo(Math.min(videoView.getCurrentPosition() + skipTime, videoView.getDuration()));
            postponeAutoHideControls();
        });

        backIB.setOnClickListener(v -> {
            final int skipTime = 10_000;
            videoView.seekTo(Math.max(videoView.getCurrentPosition() - skipTime, 0));
            postponeAutoHideControls();
        });

        nextIB.setOnClickListener(v -> {
            playVideoIdx(currentVideoIdx + 1);
            postponeAutoHideControls();
            blockNextControl();
        });
        prevIB.setOnClickListener(v -> {
            playVideoIdx(currentVideoIdx - 1);
            postponeAutoHideControls();
            blockPrevControl();
        });

        overviewIB.setOnClickListener(v -> {
            finish();
        });

        videoRL.setOnClickListener(v -> {
            if (isOpen) {
                hideControls();
                isOpen = false;
            } else {
                showControls();
                isOpen = true;
            }
        });

        var controller = getWindow().getDecorView().getWindowInsetsController();
        controller.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

//        showControls();
        hideControls();
        playVideoIdx(startVideoIdx);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playPauseIB.setImageResource(R.drawable.ic_pause);
    }

    @Override
    protected void onResume() {
        super.onResume();

        playPauseIB.setImageResource(R.drawable.ic_play);
        showControls();
    }


    private void playVideoIdx(int idx) {
        var infos = VideoRepository.getInstance().getVideoInformations();
        if (infos.isEmpty()) {
            finish();
            return;
        }

        videoView.stopPlayback();
        currentVideoIdx = idx % infos.size();
        if (currentVideoIdx < 0) {
            currentVideoIdx = infos.size() - 1;
        }

        var videoName = infos.get(currentVideoIdx).getVideoName();
        var videoPath = infos.get(currentVideoIdx).getVideoPath();
        videoNameTV.setText(videoName);
        allowPlayOnce = true;  // start playing as soon as it's prepared
        videoView.setVideoURI(Uri.parse(videoPath));
        playPauseIB.setImageResource(R.drawable.ic_pause);
    }

    private void hideControls() {
        controlsRL.setVisibility(View.GONE);

        removeAutoHideHandlers();
    }

    private void showControls() {
        controlsRL.setVisibility(View.VISIBLE);

        postponeAutoHideControls();
    }

    private void blockNextControl() {
        disableButton(nextIB);

        autoHideControlsHandler.postDelayed(blockNextRunnable, 1000);
    }

    private void blockPrevControl() {
        disableButton(prevIB);

        autoHideControlsHandler.postDelayed(blockPrevRunnable, 1000);
    }

    private void disableButton(ImageButton button) {
        button.setEnabled(false);
        button.setColorFilter(getResources().getColor(R.color.grey, null));
    }

    private void enableButton(ImageButton button) {
        button.setEnabled(true);
        button.setColorFilter(getResources().getColor(R.color.white, null));
    }

    private void postponeAutoHideControls() {
        removeAutoHideHandlers();
        autoHideControlsHandler.postDelayed(autoHideRunnable, 2000);
    }

    private void removeAutoHideHandlers() {
        autoHideControlsHandler.removeCallbacks(autoHideRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();

        removeAutoHideHandlers();
        autoHideControlsHandler.removeCallbacks(blockNextRunnable);
        autoHideControlsHandler.removeCallbacks(blockPrevRunnable);
    }

}