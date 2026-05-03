package com.example.utube.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.utube.MyApplication;
import com.example.utube.api.UserApi;
import com.example.utube.data.CommentRepository;
import com.example.utube.data.VideoRepository;
import com.example.utube.models.UserDetails;

public class UserViewModel extends ViewModel {
    private UserApi userApi;
    private MutableLiveData<Boolean> authenticateResult;
    private MutableLiveData<UserDetails> userDetails;
    private MutableLiveData<Boolean> registrationStatus;
    private MutableLiveData<Boolean> deleteUserResult;
    private MutableLiveData<Boolean> threadCreationStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> threadClosureStatus = new MutableLiveData<>();


    public LiveData<Boolean> getThreadCreationStatus() {
        return threadCreationStatus;
    }

    public void createUserThread(String token) {
        userApi.createUserThread(token, threadCreationStatus);
    }
    public LiveData<Boolean> getThreadClosureStatus() {
        return threadClosureStatus;
    }

    public void closeUserThread(String token) {
        userApi.closeUserThread(token, threadClosureStatus);
    }

    public UserViewModel() {
        authenticateResult = new MutableLiveData<>();
        userDetails = new MutableLiveData<>();
        registrationStatus = new MutableLiveData<>();
        deleteUserResult = new MutableLiveData<>();
        userApi = new UserApi(authenticateResult, userDetails, registrationStatus, deleteUserResult);
    }

    public LiveData<Boolean> getDeleteUserResult() {
        return deleteUserResult;
    }

    public MutableLiveData<Boolean> getRegistrationStatus() {
        return registrationStatus;
    }

    public LiveData<Boolean> getAuthenticateResult() {
        return authenticateResult;
    }

    public LiveData<UserDetails> getUserDetails() {
        return userDetails;
    }

    public void deleteUser(String userId, String token) {
        userApi.deleteUser(userId, token);
    }
    public void deleteUserLocalData(String username) {
        new Thread(() -> {
            VideoRepository videoRepository = new VideoRepository(MyApplication.getInstance());
            CommentRepository commentRepository = new CommentRepository(MyApplication.getInstance());
            videoRepository.deleteAllVideosByAuthor(username);
            commentRepository.deleteAllCommentsByUsername(username);
        }).start();
    }

    public void authenticate(String username, String password) {
        userApi.authenticate(username, password);
    }

    public void fetchUserDetails(UserDetails user) {
        userApi.fetchUserDetails(user);
    }

    public void signUp(UserDetails user) {
        userApi.signUp(user);
    }

    public void updateUserDetails(UserDetails user) {
        userApi.updateUserDetails(user);
    }

    public void notifyVideoWatch(String videoId, String token) {
        userApi.notifyVideoWatch(videoId, token);
    }

}
