package de.markusseidl.nuckelplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VideoRepository {
    private static VideoRepository instance;
    private List<VideoInformation> videoInformations;

    private Set<String> videoPathSet;

    public VideoRepository() {
        this.videoInformations = Collections.synchronizedList(new ArrayList<>());
        this.videoPathSet = Collections.synchronizedSet(new HashSet<>());
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

    public boolean add(VideoInformation videoInformation) {
        if (videoPathSet.contains(videoInformation.getVideoPath())) {
            return false;
        }

        videoInformations.add(videoInformation);
        videoPathSet.add(videoInformation.getVideoPath());
        return true;
    }

    public List<VideoInformation> getVideoInformations() {
        return videoInformations;
    }
}
