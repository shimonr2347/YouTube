package com.example.utube.activities;

import android.app.Application;
import android.util.Log;

import com.example.utube.data.VideoRepository;
import com.example.utube.models.Video;
import com.example.utube.models.VideoEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class VideoManager {
    private static VideoManager instance;
    private VideoRepository videoRepository;
    private List<Video> filteredVideoList = new ArrayList<>();
    private Map<String, Boolean> likedStateMap = new HashMap<>();
    private Map<String, Integer> likesCountMap = new HashMap<>();
    private ExecutorService executorService;

    private VideoManager(Application application) {
        videoRepository = new VideoRepository(application);
    }

    public static synchronized VideoManager getInstance(Application application) {
        if (instance == null) {
            instance = new VideoManager(application);
        }
        return instance;
    }

    public List<Video> getVideoList() {
        List<VideoEntity> entities = videoRepository.getAllVideos();
        List<Video> videos = entities.stream().map(this::entityToVideo).collect(Collectors.toList());
        Log.d("VideoManager", "Fetched " + videos.size() + " videos");
        return videos;
    }


    public List<Video> getFilteredVideoList() {
        Log.d("VideoManager", "FilteredVideoList size: " + filteredVideoList.size());
        if (filteredVideoList.isEmpty()) {
            Log.d("VideoManager", "FilteredVideoList is empty, returning all videos");
            return getVideoList();
        }
        return filteredVideoList;
    }

    public void clearFilteredList() {
        filteredVideoList.clear();
    }

    public Map<String, Video> getVideoMap() {
        Map<String, Video> videoMap = new HashMap<>();
        for (Video video : getVideoList()) {
            videoMap.put(video.getId(), video);
        }
        return videoMap;
    }

    public Map<String, Boolean> getLikedStateMap() {
        return likedStateMap;
    }

    public Map<String, Integer> getLikesCountMap() {
        return likesCountMap;
    }

public void addVideo(Video video) {
    Log.d("VideoManager", "Adding video: " + video.getId());
    VideoEntity existingVideo = videoRepository.getVideoById(video.getId());
    if (existingVideo == null) {
        videoRepository.insert(videoToEntity(video));
    } else {
        videoRepository.updateVideo(videoToEntity(video));
    }
    filteredVideoList.add(video);
    List<Video> updatedList = getVideoList();
    Log.d("VideoManager", "After add list size: " + updatedList.size());
    filteredVideoList.clear();
    filteredVideoList.addAll(updatedList);
}

    public void removeVideo(String videoId) {
        Log.d("VideoManager", "Deleting video: " + videoId);
        VideoEntity entity = videoRepository.getVideoById(videoId);
        if (entity != null) {
            videoRepository.deleteVideo(entity);
            List<Video> updatedList = getVideoList();
            Log.d("VideoManager", "after delete list size: " + updatedList.size());
            filteredVideoList.clear();
            filteredVideoList.addAll(updatedList);
        }
    }

    public void updateVideo(Video video) {
        Log.d("VideoManager", "Updating video: " + video.getId());
        videoRepository.updateVideo(videoToEntity(video));
        Log.d("VideoManager", "Updating video " + video.getId() + " with views: " + video.getViews());

        List<Video> updatedList = getVideoList();
        Log.d("VideoManager", "Updated list size: " + updatedList.size());
        filteredVideoList.clear();
        filteredVideoList.addAll(updatedList);
    }

    public void setVideoList(List<Video> videos) {
        for (Video video : videos) {
            VideoEntity existingVideo = videoRepository.getVideoById(video.getId());
            if (existingVideo == null) {
                videoRepository.insert(videoToEntity(video));
            } else {
                videoRepository.updateVideo(videoToEntity(video));
            }
        }
        filteredVideoList.clear();
        filteredVideoList.addAll(videos);
        Log.d("VideoManager", "setVideoList: filteredVideoList size after update: " + filteredVideoList.size());
    }

    public List<Video> getVideosForAuthor(String author) {
        List<VideoEntity> entities = videoRepository.getVideosForAuthor(author);
        List<Video> videos = entities.stream().map(this::entityToVideo).collect(Collectors.toList());
        Log.d("VideoManager", "Fetched " + videos.size() + " videos for author: " + author);
        return videos;
    }

    public Video getVideoById(String id) {
        VideoEntity entity = videoRepository.getVideoById(id);
        return entity != null ? entityToVideo(entity) : null;
    }

    private Video entityToVideo(VideoEntity entity) {
        return new Video(entity.id, entity.title, entity.author, entity.views, entity.uploadTime,
                entity.thumbnailUrl, entity.authorProfilePicUrl, entity.videoUrl,
                entity.category, entity.likes);
    }

    private VideoEntity videoToEntity(Video video) {
        return new VideoEntity(video.getId(), video.getTitle(), video.getAuthor(), video.getViews(),
                video.getUploadTime(), video.getThumbnailUrl(), video.getAuthorProfilePicUrl(),
                video.getVideoUrl(), video.getCategory(), video.getLikes());
    }

    public void incrementViews(String videoId) {
        executorService.execute(() -> {
            VideoEntity entity = videoRepository.getVideoById(videoId);
            if (entity != null) {
                entity.setViews(entity.getViews() + 1);
                videoRepository.updateVideo(entity);
                Log.d("VideoManager", "Incremented views for video " + videoId + ": " + entity.getViews());
            }
        });
    }
}