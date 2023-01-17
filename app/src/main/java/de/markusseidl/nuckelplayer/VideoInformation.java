package de.markusseidl.nuckelplayer;

import android.graphics.Bitmap;

public class VideoInformation {
    private String videoName;
    private String videoPath;
    private Bitmap thumbnail;

    public VideoInformation(String videoName, String videoPath, Bitmap thumbnail) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.thumbnail = thumbnail;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }
}
