package com.example.utube.utils;

public class CommentResponse {
    private int id;
    private String text;
    private String username;
    private String uploadTime;
    private int likes;
    private String profilePicUrl;
    private String _id;

    public CommentResponse(int id, String text, String username, String uploadTime, int likes, String profilePicUrl, String _id) {
        this.id = id;
        this.text = text;
        this.username = username;
        this.uploadTime = uploadTime;
        this.likes = likes;
        this.profilePicUrl = profilePicUrl;
        this._id = _id;
    }
    public String getServerId() {
        return _id;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public int getLikes() {
        return likes;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }


}