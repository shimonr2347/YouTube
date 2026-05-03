package com.example.utube.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.utube.api.VideoApi;

import java.io.File;

public class AddVideoViewModel extends AndroidViewModel {
    private MutableLiveData<Uri> selectedImageUri = new MutableLiveData<>();
    private MutableLiveData<String[]> categories = new MutableLiveData<>();
    private MutableLiveData<Boolean> uploadStatus = new MutableLiveData<>();
    private VideoApi videoApi;

    public AddVideoViewModel(Application application) {
        super(application);
        loadCategories();
        videoApi = new VideoApi(uploadStatus, null, null);
    }

    private void loadCategories() {
        String[] categoriesArray = {"Sport", "News", "Cinema", "Gaming"};
        categories.setValue(categoriesArray);
    }

    public void setCategories(MutableLiveData<String[]> categories) {
        this.categories = categories;
    }

    public void setUploadStatus(MutableLiveData<Boolean> uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public void setSelectedImageUri(Uri uri) {
        selectedImageUri.setValue(uri);
    }

    public LiveData<Uri> getSelectedImageUri() {
        return selectedImageUri;
    }

    public LiveData<String[]> getCategories() {
        return categories;
    }

    public void uploadVideo(String title, String category, File videoFile, File thumbnailFile, String userId, String authorName, String token) {
        if (videoFile != null && thumbnailFile != null) {
            videoApi.uploadVideo(title, category, videoFile, thumbnailFile, userId, authorName, token);
        } else {
            uploadStatus.postValue(false);
        }
    }
    public boolean isInputValid(String title, String category) {
        return !title.isEmpty() && !category.isEmpty() && selectedImageUri.getValue() != null;
    }

    public MutableLiveData<Boolean> getUploadStatus() {
        return uploadStatus;
    }

}
