package com.example.utube.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.utube.MyApplication;
import com.example.utube.activities.MainActivity;
import com.example.utube.activities.VideoManager;
import com.example.utube.api.RetrofitClient;
import com.example.utube.api.WebServiceApi;
import com.example.utube.data.VideoRepository;
import com.example.utube.models.CommentEntity;
import com.example.utube.models.Video;
import com.example.utube.data.CommentRepository;
import com.example.utube.utils.CommentRequest;
import com.example.utube.utils.CommentResponse;
import com.example.utube.utils.VideoResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoDetailViewModel extends AndroidViewModel {
    private VideoManager videoManager;
    private CommentRepository commentRepository;
    private MutableLiveData<Video> video = new MutableLiveData<>();
    private MutableLiveData<List<Video.Comment>> comments = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLiked = new MutableLiveData<>();

    private SharedPreferences sharedPreferences;
    private MutableLiveData<String> error = new MutableLiveData<>();
    private VideoRepository videoRepository = new VideoRepository(MyApplication.getInstance());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<List<Video>> recommendedVideos = new MutableLiveData<>();



    public VideoDetailViewModel(Application application) {
        super(application);
        isLoading = new MutableLiveData<>(false);
        videoManager = VideoManager.getInstance(application);
        commentRepository = new CommentRepository(application);
        recommendedVideos = new MutableLiveData<>();
        sharedPreferences = application.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void loadVideo(String videoId) {
        Video loadedVideo = videoManager.getVideoById(videoId);
        if (loadedVideo != null) {
            loadedVideo.setViews(loadedVideo.getViews() + 1);
            videoManager.updateVideo(loadedVideo);
            video.postValue(loadedVideo);
            Log.d("VideoDetailViewModel", "Updated views for video " + videoId + ": " + loadedVideo.getViews());
        }
    }

    public LiveData<List<Video>> getRecommendedVideos() {
        return recommendedVideos;
    }

    public LiveData<String> getError() {
        return error;
    }
    public void fetchRecommendedVideos(String token, String videoId) {
        Log.d("VideoDetailViewModel", "Fetching recommendations - Token: " + token + ", VideoId: " + videoId);


        if (videoId == null) {
            error.postValue("Invalid video ID");
            return;
        }

        // Use "guest" as token if null or empty
        String actualToken = (token == null || token.isEmpty()) ? "guest" : token;
        //log the actual token
        Log.d("VideoDetailViewModel", "Actual token: " + actualToken);

        videoRepository.fetchRecommendedVideosFromServer(actualToken, videoId, new Callback<List<VideoResponse>>() {
            @Override
            public void onResponse(Call<List<VideoResponse>> call, Response<List<VideoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = convertToVideoList(response.body());
                    recommendedVideos.postValue(videos);
                } else {
                    error.postValue("Failed to load recommended videos");
                }
            }

            @Override
            public void onFailure(Call<List<VideoResponse>> call, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }
    public void fetchVideoDetailsFromServer(String videoId, Callback<VideoResponse> callback) {
        videoRepository.fetchVideoDetailsFromServer(videoId, new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VideoResponse videoResponse = response.body();
                    Log.d("MainViewModel", "Received video details: " + new Gson().toJson(videoResponse));
                    Video video = convertVideoResponseToVideo(videoResponse);
                    videoRepository.updateVideoFromModel(video);
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e("MainViewModel", "Error fetching video details", t);
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
                    response.getVideoUrl(),
                    response.getCategory(),
                    response.getLikes()
            );
            videoList.add(video);
        }
        return videoList;
    }

    public void addComment(String videoId, String username, String text, String profilePicUrl, String date, String serverId) {
        String currentTime = date;
        CommentEntity newComment = new CommentEntity(videoId, username, text, currentTime, 0, profilePicUrl, serverId);
        long commentId = commentRepository.insert(newComment);

        if (commentId != -1) {
            newComment.setId((int) commentId);
            List<Video.Comment> currentComments = comments.getValue();
            if (currentComments == null) {
                currentComments = new ArrayList<>();
            }
            Video.Comment addedComment = convertToVideoComment(newComment);
            currentComments.add(addedComment);
            comments.postValue(new ArrayList<>(currentComments)); // Create a new list to trigger update
            Log.d("VideoDetailViewModel", "Added comment: " + addedComment.getId() + ", " + addedComment.getText());
        } else {
            Log.e("VideoDetailViewModel", "Failed to add comment");
        }
    }

    public void updateLikesCount(boolean isLiked) {
        Video currentVideo = video.getValue();
        if (currentVideo != null) {
            int currentLikes = currentVideo.getLikes();
            currentVideo.setLikes(isLiked ? currentLikes + 1 : currentLikes - 1);
            videoManager.updateVideo(currentVideo);
            video.postValue(currentVideo);
        }
    }

    private Video.Comment convertToVideoComment(CommentEntity entity) {
        return new Video.Comment(entity.getId(), entity.getUsername(), entity.getText(),
                entity.getUploadTime(), entity.getLikes(), entity.getProfilePicUrl(), entity.getServerId());
    }

    public void loadComments(String videoId) {
        List<CommentEntity> loadedComments = commentRepository.getCommentsForVideo(videoId);
        List<Video.Comment> convertedComments = new ArrayList<>();
        for (CommentEntity entity : loadedComments) {
            convertedComments.add(convertToVideoComment(entity));
        }
        comments.postValue(convertedComments);
    }

    public void updateLikeStatus(String videoId, String currentLoggedInUser, boolean liked) {
        String likeKey = videoId + "_" + currentLoggedInUser + "_liked";
        sharedPreferences.edit().putBoolean(likeKey, liked).apply();
        isLiked.postValue(liked);

        Video video = videoManager.getVideoById(videoId);
        if (video != null) {
            int currentLikes = video.getLikes();
            video.setLikes(liked ? currentLikes + 1 : currentLikes - 1);
            videoManager.updateVideo(video);
            this.video.postValue(video);
        }
    }

    public boolean isVideoLiked(String videoId, String currentLoggedInUser) {
        String likeKey = videoId + "_" + currentLoggedInUser + "_liked";
        return sharedPreferences.getBoolean(likeKey, false);
    }


    public void incrementViews() {
        Video currentVideo = video.getValue();
        if (currentVideo != null) {
            currentVideo.setViews(currentVideo.getViews() + 1);
            videoManager.updateVideo(currentVideo);
            video.postValue(currentVideo);
        }
    }

    public void updateCommentLikeStatus(String videoId, int commentId, String currentLoggedInUser, boolean liked) {
        String likeKey = videoId + "_" + commentId + "_" + currentLoggedInUser + "_liked";
        sharedPreferences.edit().putBoolean(likeKey, liked).apply();

        List<Video.Comment> currentComments = comments.getValue();
        if (currentComments != null) {
            for (Video.Comment comment : currentComments) {
                if (comment.getId() == commentId) {
                    int currentLikes = comment.getLikes();
                    comment.setLikes(liked ? currentLikes + 1 : currentLikes - 1);
                    break;
                }
            }
            comments.postValue(currentComments);
        }

        // Update the comment in the database
        CommentEntity commentEntity = commentRepository.getCommentById(commentId);
        if (commentEntity != null) {
            commentEntity.setLikes(liked ? commentEntity.getLikes() + 1 : commentEntity.getLikes() - 1);
            commentRepository.updateComment(commentEntity);
        }
    }

    public boolean isCommentLiked(String videoId, int commentId, String currentLoggedInUser) {
        String likeKey = videoId + "_" + commentId + "_" + currentLoggedInUser + "_liked";
        return sharedPreferences.getBoolean(likeKey, false);
    }

    public VideoManager getVideoManager() {
        return videoManager;
    }

    public CommentRepository getCommentRepository() {
        return commentRepository;
    }

    public LiveData<Video> getVideo() {
        return video;
    }

    public LiveData<List<Video.Comment>> getComments() {
        return comments;
    }

    public LiveData<Boolean> getIsLiked() {
        return isLiked;
    }

    public interface ToggleLikeCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    public void toggleLikeOnServer(String videoId, String userId, String token, boolean isLiking, ToggleLikeCallback callback) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        Call<VideoResponse> call = isLiking ?
                api.likeVideo(userId, videoId, "Bearer " + token) :
                api.unlikeVideo(userId, videoId, "Bearer " + token);

        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VideoResponse updatedVideo = response.body();
                    Log.d("VideoDetailViewModel", "Like toggled successfully on server. New like count: " + updatedVideo.getLikes());
                    callback.onSuccess();
                } else {
                    Log.e("VideoDetailViewModel", "Error toggling like on server. Response code: " + response.code());
                    callback.onError("Failed to update like status on server. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e("VideoDetailViewModel", "Network error when toggling like", t);
                callback.onError("Network error. Please check your connection and try again.");
            }
        });
    }

    public void addCommentToServer(String videoId, String userId, String text, String token, AddCommentCallback callback) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        CommentRequest request = new CommentRequest(text);
        Call<CommentResponse> call = api.addComment(userId, videoId, request, "Bearer " + token);

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    Log.d("AddComment", "Server Response: " + jsonResponse);
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add comment");
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface AddCommentCallback {
        void onSuccess(CommentResponse comment);

        void onError(String message);
    }

    public interface ToggleCommentLikeCallback {
        void onSuccess(CommentResponse updatedComment);

        void onError(String message);
    }

    public void toggleCommentLikeOnServer(String videoId, String commentId, String userId, String token, boolean isLiking, ToggleCommentLikeCallback callback) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        Call<CommentResponse> call = isLiking ?
                api.likeComment(userId, videoId, commentId, "Bearer " + token) :
                api.unlikeComment(userId, videoId, commentId, "Bearer " + token);

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommentResponse updatedComment = response.body();
                    Log.d("VideoDetailViewModel", "Comment like toggled successfully on server. New like count: " + updatedComment.getLikes());
                    callback.onSuccess(updatedComment);
                } else {
                    Log.e("VideoDetailViewModel", "Error toggling comment like on server. Response code: " + response.code());
                    callback.onError("Failed to update comment like status on server. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Log.e("VideoDetailViewModel", "Network error when toggling comment like", t);
                callback.onError("Network error. Please check your connection and try again.");
            }
        });
    }

    public interface UpdateCommentCallback {
        void onSuccess(CommentResponse updatedComment);

        void onError(String message);
    }

    public void updateCommentOnServer(String videoId, String commentId, String userId, String text, String token, UpdateCommentCallback callback) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        CommentRequest request = new CommentRequest(text);
        Call<CommentResponse> call = api.updateComment(userId, videoId, commentId, request, "Bearer " + token);

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("VideoDetailViewModel", "Comment updated successfully on server.");
                    callback.onSuccess(response.body());
                } else {
                    Log.e("VideoDetailViewModel", "Error updating comment on server. Response code: " + response.code());
                    callback.onError("Failed to update comment on server. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Log.e("VideoDetailViewModel", "Network error when updating comment", t);
                callback.onError("Network error. Please check your connection and try again.");
            }
        });
    }

    public void updateComment(CommentEntity updatedComment) {
        commentRepository.updateComment(updatedComment);

        List<Video.Comment> currentComments = comments.getValue();
        if (currentComments != null) {
            List<Video.Comment> newComments = new ArrayList<>(currentComments);
            for (int i = 0; i < newComments.size(); i++) {
                if (newComments.get(i).getId() == updatedComment.getId()) {
                    Video.Comment updated = convertToVideoComment(updatedComment);
                    newComments.set(i, updated);
                    Log.d("VideoDetailViewModel", "Updated comment: " + updated.getId() + ", " + updated.getText());
                    break;
                }
            }
            comments.postValue(newComments);
        }
    }

    public interface DeleteCommentCallback {
        void onSuccess();

        void onError(String message);
    }

    public void deleteCommentOnServer(String videoId, String commentId, String userId, String token, DeleteCommentCallback callback) {
        WebServiceApi api = RetrofitClient.getInstance().create(WebServiceApi.class);
        Call<Void> call = api.deleteComment(userId, videoId, commentId, "Bearer " + token);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("VideoDetailViewModel", "Comment deleted successfully on server.");
                    callback.onSuccess();
                } else {
                    Log.e("VideoDetailViewModel", "Error deleting comment on server. Response code: " + response.code());
                    callback.onError("Failed to delete comment on server. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("VideoDetailViewModel", "Network error when deleting comment", t);
                callback.onError("Network error. Please check your connection and try again.");
            }
        });
    }

    public void deleteComment(CommentEntity comment) {
        commentRepository.deleteComment(comment);
        loadComments(comment.getVideoId()); // Reload comments after deleting
    }

    public void refreshComments(String videoId) {
        videoRepository.fetchCommentsFromServer(videoId, new Callback<List<CommentResponse>>() {
            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //log the response
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    Log.d("VideoDetailViewModel", "comments response: " + jsonResponse);
                    List<CommentEntity> commentEntities = convertToCommentEntities(response.body(), videoId);
                    commentRepository.mergeCommentsForVideo(videoId, commentEntities);
                    loadComments(videoId);
                }
            }

            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                // Handle error
                Log.e("VideoDetailViewModel", "Failed to fetch comments", t);
            }
        });
    }

    private List<CommentEntity> convertToCommentEntities(List<CommentResponse> responses, String videoId) {
        List<CommentEntity> entities = new ArrayList<>();
        for (CommentResponse response : responses) {
            CommentEntity entity = new CommentEntity(videoId, response.getUsername(), response.getText(),
                    response.getUploadTime(), response.getLikes(), response.getProfilePicUrl(), response.getServerId());
            //log the profile pic
            Log.d("VideoDetailViewModel", "Profile pic url: " + response.getProfilePicUrl());
            entities.add(entity);
        }
        return entities;
    }


}