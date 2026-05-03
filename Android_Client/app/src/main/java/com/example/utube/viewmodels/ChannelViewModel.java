package com.example.utube.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.utube.activities.VideoManager;
import com.example.utube.data.VideoRepository;
import com.example.utube.models.UserDetails;
import com.example.utube.models.Video;
import com.example.utube.utils.VideoResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChannelViewModel extends AndroidViewModel {
    private VideoManager videoManager;
    private MutableLiveData<List<Video>> videos = new MutableLiveData<>();
    private VideoRepository videoRepository;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public ChannelViewModel(Application application) {
        super(application);
        videoManager = VideoManager.getInstance(application);
        videoRepository = new VideoRepository(application);
    }

    public void loadVideosForAuthor(String authorName) {
        isLoading.setValue(true);
        videoRepository.fetchVideosByUsernameFromServer(authorName, new Callback<List<VideoResponse>>() {
            @Override
            public void onResponse(Call<List<VideoResponse>> call, Response<List<VideoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> newVideos = convertToVideoList(response.body());
                    videos.postValue(newVideos);
                    Log.d("ChannelViewModel", "Loaded " + newVideos.size() + " videos for author: " + authorName);
                } else {
                    error.postValue("Failed to load videos. Please try again.");
                    Log.e("ChannelViewModel", "Failed to load videos for author: " + authorName);
                    loadVideosFromLocal(authorName);
                }
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Call<List<VideoResponse>> call, Throwable t) {
                error.postValue("Network error. Please check your connection and try again.");
                Log.e("ChannelViewModel", "Network error loading videos for author: " + authorName, t);
                loadVideosFromLocal(authorName);
                isLoading.postValue(false);
            }
        });
    }

    private void loadVideosFromLocal(String authorName) {
        List<Video> localVideos = videoManager.getVideosForAuthor(authorName);
        videos.postValue(localVideos);
        Log.d("ChannelViewModel", "Loaded " + localVideos.size() + " local videos for author: " + authorName);
    }

    private List<Video> convertToVideoList(List<VideoResponse> videoResponses) {
        List<Video> videoList = new ArrayList<>();
        for (VideoResponse response : videoResponses) {
            Video video = new Video(
                    response.getId(),
                    response.getTitle(),
                    response.getAuthor(),
                    response.getViews(),
                    response.getUploadTime(),
                    response.getThumbnailUrl(),
                    response.getAuthorProfilePic(),
                    response.getVideoUrl() != null ? response.getVideoUrl() : "",
                    response.getCategory(),
                    response.getLikes()
            );
            videoList.add(video);
        }
        return videoList;
    }

    public LiveData<List<Video>> getVideos() {
        return videos;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchVideoDetailsFromServer(String videoId, Callback<VideoResponse> callback) {
        videoRepository.fetchVideoDetailsFromServer(videoId, new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VideoResponse videoResponse = response.body();
                    Log.d("ChannelViewModel", "Received video details: " + new Gson().toJson(videoResponse));
                    Video video = convertVideoResponseToVideo(videoResponse);
                    videoRepository.updateVideoFromModel(video);
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e("ChannelViewModel", "Error fetching video details", t);
                callback.onFailure(call, t);
            }
        });
    }

    private Video convertVideoResponseToVideo(VideoResponse videoResponse) {
        return new Video(
                videoResponse.getId(),
                videoResponse.getTitle(),
                videoResponse.getAuthor(),
                videoResponse.getViews(),
                videoResponse.getUploadTime(),
                videoResponse.getThumbnailUrl(),
                videoResponse.getAuthorProfilePic(),
                videoResponse.getVideoUrl(),
                videoResponse.getCategory(),
                videoResponse.getLikes()
        );
    }
    public void removeVideo(String videoId) {
        videoManager.removeVideo(videoId);
        loadVideosForAuthor(UserDetails.getInstance().getUsername());
    }


}