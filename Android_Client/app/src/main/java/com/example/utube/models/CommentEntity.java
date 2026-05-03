package com.example.utube.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class CommentEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String videoId;
    public String username;
    public String text;
    public String uploadTime;
    public int likes;
    public String profilePicUrl;
    @ColumnInfo(name = "server_id")
    public String serverId; //try-com-id

    // Constructor
    public CommentEntity(String videoId, String username, String text, String uploadTime,
                         int likes, String profilePicUrl, String serverId) {
        this.videoId = videoId;
        this.username = username;
        this.text = text;
        this.uploadTime = uploadTime;
        this.likes = likes;
        this.profilePicUrl = profilePicUrl;
        this.serverId = serverId;
    }
    public String getServerId() {
        return serverId;
    }
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}