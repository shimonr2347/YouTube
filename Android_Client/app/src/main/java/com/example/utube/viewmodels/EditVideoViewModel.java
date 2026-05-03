package com.example.utube.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.utube.activities.VideoManager;
import com.example.utube.api.VideoApi;
import com.example.utube.models.UserDetails;
import com.example.utube.models.Video;

import java.io.File;

public class EditVideoViewModel extends AndroidViewModel {
    private MutableLiveData<Video> video = new MutableLiveData<>();
    private MutableLiveData<Uri> newVideoUri = new MutableLiveData<>();
    private MutableLiveData<String[]> categories = new MutableLiveData<>();
    private MutableLiveData<Boolean> editVideoResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteVideoResult = new MutableLiveData<>();
    private VideoApi videoApi;


    public EditVideoViewModel(Application application) {
        super(application);
        loadCategories();
        videoApi = new VideoApi(null, editVideoResult, deleteVideoResult);
    }

    public LiveData<Boolean> getDeleteVideoResult() {
        return deleteVideoResult;
    }

    public void deleteVideo(String videoId) {
        String userId = UserDetails.getInstance().get_id();
        String token = UserDetails.getInstance().getToken();
        videoApi.deleteVideo(videoId, userId, token);
    }

    public MutableLiveData<Uri> getNewVideoUri() {
        return newVideoUri;
    }

    public MutableLiveData<Boolean> getEditVideoResult() {
        return editVideoResult;
    }


    public void setVideo(MutableLiveData<Video> video) {
        this.video = video;
    }

    public void setNewVideoUri(MutableLiveData<Uri> newVideoUri) {
        this.newVideoUri = newVideoUri;
    }

    public void setCategories(MutableLiveData<String[]> categories) {
        this.categories = categories;
    }

    public void setEditVideoResult(MutableLiveData<Boolean> editVideoResult) {
        this.editVideoResult = editVideoResult;
    }

    private void loadCategories() {
        String[] categoriesArray = {"Sport", "News", "Cinema", "Gaming"};
        categories.setValue(categoriesArray);
    }

    public void loadVideo(String videoId) {
        Video loadedVideo = VideoManager.getInstance(getApplication()).getVideoMap().get(videoId);
        video.setValue(loadedVideo);
    }

    public void setNewVideoUri(Uri uri) {
        newVideoUri.setValue(uri);
        Log.d("EditVideoViewModel", "setNewVideoUri: " + uri.toString());
    }

    public void saveChanges(String newTitle, String newCategory) {
        Video currentVideo = video.getValue();
        if (currentVideo != null) {
            currentVideo.setTitle(newTitle);
            currentVideo.setCategory(newCategory);
            if (newVideoUri.getValue() != null) {
                currentVideo.setVideoUrl(newVideoUri.getValue().toString());
            }
            VideoManager.getInstance(getApplication()).updateVideo(currentVideo);
            video.setValue(currentVideo);
        }
    }

    public void editVideo(String videoId, String title, String category, Uri videoUri, String userId, String token) {
        videoApi.editVideo(videoId, title, category, videoUri, userId, token);
    }

    public MutableLiveData<Video> getVideo() {
        return video;
    }

    public MutableLiveData<String[]> getCategories() {
        return categories;
    }

}
