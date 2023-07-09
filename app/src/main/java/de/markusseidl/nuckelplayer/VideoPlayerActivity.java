package de.markusseidl.nuckelplayer;

import androidx.appcompat.app.AppCompatActivity;

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

    private RelativeLayout videoRL;
    private int currentVideoIdx;

    private boolean allowPlayOnce = true;

    private ControlStateHelper controlState;

    private int lastKnownSeekPosition = 0;

    private final Runnable blockNextRunnable = new Runnable() {
        @Override
        public void run() {
            enableButton(nextIB);
            controlState.autoHideControlsHandler.removeCallbacks(blockNextRunnable);
        }
    };

    private final Runnable blockPrevRunnable = new Runnable() {
        @Override
        public void run() {
            enableButton(prevIB);
            controlState.autoHideControlsHandler.removeCallbacks(blockPrevRunnable);
        }
    };

    private final Runnable blockPositionSaver = new Runnable() {
        @Override
        public void run() {
            if (videoView.isPlaying()) {
                lastKnownSeekPosition = videoView.getCurrentPosition();
                System.out.println("Last known position: " + lastKnownSeekPosition);
            }
            positionSaver.postDelayed(blockPositionSaver, 500);
        }
    };
    private Handler positionSaver;

    private boolean videoEnded;

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
        videoRL = findViewById(R.id.idRLVideo);
        var controlsRL = findViewById(R.id.idRLControls);
        controlState = new ControlStateHelper((RelativeLayout) controlsRL);
        positionSaver = new Handler(Looper.getMainLooper());
        positionSaver.postDelayed(blockPositionSaver, 500);


        videoView.setOnCompletionListener(mp -> {
            controlState.onPause();
            playPauseIB.setImageResource(R.drawable.ic_play);
            videoEnded = true;
        });

        videoView.setOnPreparedListener(mp -> {
            System.out.println("Video prepared");
            System.out.println("Last known position: " + lastKnownSeekPosition);
            videoView.seekTo(lastKnownSeekPosition);
            if (allowPlayOnce) {
                videoView.start();
                controlState.onPlay();
                allowPlayOnce = false;
                playPauseIB.setImageResource(R.drawable.ic_pause);
            } else {
                controlState.onPause();
            }
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            System.out.println("Error: " + what + " " + extra);
            finish();
            return false;
        });

        playPauseIB.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                controlState.onPause();
                playPauseIB.setImageResource(R.drawable.ic_play);
            } else {
                // if the video is at the end, let play reset to the beginning of the clip
                System.out.println("Current position: " + videoView.getCurrentPosition() + " Duration: " + videoView.getDuration());

                if(videoEnded) {
                    videoView.seekTo(0);
                    lastKnownSeekPosition = 0;
                    videoEnded = false;
                } else if(Math.abs(lastKnownSeekPosition - videoView.getCurrentPosition()) > 1000) {
                    videoView.seekTo(lastKnownSeekPosition);
                }
                videoView.start();
                controlState.onPlay();
                System.out.println("Last known position: " + lastKnownSeekPosition);
                playPauseIB.setImageResource(R.drawable.ic_pause);
            }
        });
        playPauseIB.setImageResource(R.drawable.ic_pause);

        forwardIB.setOnClickListener(v -> {
            final int skipTime = 10_000;
            if (videoView.getCurrentPosition() + skipTime > videoView.getDuration()) {
                //playVideoIdx(currentVideoIdx + 1);
            } else {
                videoView.seekTo(videoView.getCurrentPosition() + skipTime);
                if (!videoView.isPlaying()) {
                    // If video is paused update position, bc seekTo doesn't fire update when
                    // video is paused
                    lastKnownSeekPosition += skipTime;
                }
            }
            controlState.postponeAutoHideControls();
        });

        backIB.setOnClickListener(v -> {
            videoEnded = false;
            final int skipTime = 10_000;
            int newPosition = videoView.getCurrentPosition() - skipTime;
            if (newPosition < 0) {
                newPosition = 0;
            }
            videoView.seekTo(newPosition);
            if (!videoView.isPlaying()) {
                // If video is paused update position, bc seekTo doesn't fire update when
                // video is paused
                lastKnownSeekPosition = newPosition;
            }
            controlState.postponeAutoHideControls();
        });

        nextIB.setOnClickListener(v -> {
            videoEnded = false;
            playVideoIdx(currentVideoIdx + 1);
            controlState.postponeAutoHideControls();
            blockNextControl();
        });
        prevIB.setOnClickListener(v -> {
            videoEnded = false;
            playVideoIdx(currentVideoIdx - 1);
            controlState.postponeAutoHideControls();
            blockPrevControl();
        });

        overviewIB.setOnClickListener(v -> {
            finish();
        });

        videoRL.setOnClickListener(v -> {
            controlState.onTap();
        });

        var controller = getWindow().getDecorView().getWindowInsetsController();
        controller.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

//        showControls();
        controlState.hideControls();
        playVideoIdx(startVideoIdx);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");

        playPauseIB.setImageResource(R.drawable.ic_pause);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");

        playPauseIB.setImageResource(R.drawable.ic_play);
        controlState.showControls();
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
        lastKnownSeekPosition = 0;
        videoView.setVideoURI(Uri.parse(videoPath));
        playPauseIB.setImageResource(R.drawable.ic_pause);
    }


    private void blockNextControl() {
        disableButton(nextIB);

        controlState.autoHideControlsHandler.postDelayed(blockNextRunnable, 1000);
    }

    private void blockPrevControl() {
        disableButton(prevIB);

        controlState.autoHideControlsHandler.postDelayed(blockPrevRunnable, 1000);
    }

    private void disableButton(ImageButton button) {
        button.setEnabled(false);
        button.setColorFilter(getResources().getColor(R.color.grey, null));
    }

    private void enableButton(ImageButton button) {
        button.setEnabled(true);
        button.setColorFilter(getResources().getColor(R.color.white, null));
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");

        controlState.removeAutoHideHandlers();
        controlState.autoHideControlsHandler.removeCallbacks(blockNextRunnable);
        controlState.autoHideControlsHandler.removeCallbacks(blockPrevRunnable);
    }

    public static class ControlStateHelper {
        private RelativeLayout controlsRL;
        protected Handler autoHideControlsHandler;
        private boolean isPlaying;
        private boolean isOpen;
        private final Runnable autoHideRunnable = new Runnable() {
            @Override
            public void run() {
                hideControls();
                autoHideControlsHandler.removeCallbacks(autoHideRunnable);
            }
        };

        public ControlStateHelper(RelativeLayout controlsRL) {
            this.controlsRL = controlsRL;
            autoHideControlsHandler = new Handler(Looper.getMainLooper());
        }

        protected void onPause() {
            isPlaying = false;
            showControls();
        }

        protected void onPlay() {
            isPlaying = true;

            hideControls();
        }

        protected void hideControls() {
            isOpen = false;
            controlsRL.setVisibility(View.GONE);

            removeAutoHideHandlers();
        }

        protected void showControls() {
            isOpen = true;
            controlsRL.setVisibility(View.VISIBLE);

            if (isPlaying) {
                postponeAutoHideControls();
            } else {
                removeAutoHideHandlers();
            }
        }


        private void postponeAutoHideControls() {
            removeAutoHideHandlers();
            autoHideControlsHandler.postDelayed(autoHideRunnable, 2000);
        }

        private void removeAutoHideHandlers() {
            autoHideControlsHandler.removeCallbacks(autoHideRunnable);
        }

        public void onTap() {
            if (isPlaying) {
                if (isOpen) {
                    hideControls();
                } else {
                    showControls();
                }
            }
        }
    }
}