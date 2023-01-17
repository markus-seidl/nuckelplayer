package de.markusseidl.nuckelplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoRepository {
    private static VideoRepository instance;
    private List<VideoInformation> videoInformations;

    public VideoRepository() {
        this.videoInformations = Collections.synchronizedList(new ArrayList<>());
    }

    public static VideoRepository getInstance() {
        if (instance == null) {
            synchronized (VideoRepository.class) {
                if (instance == null) {
                    instance = new VideoRepository();
                }
            }
        }
        return instance;
    }

    public void add(VideoInformation videoInformation) {
        videoInformations.add(videoInformation);
    }

    public List<VideoInformation> getVideoInformations() {
        return videoInformations;
    }
}
