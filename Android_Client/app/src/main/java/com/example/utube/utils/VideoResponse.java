package com.example.utube.utils;

import com.google.gson.annotations.SerializedName;

public class VideoResponse {
    @SerializedName("_id")
    private String id;
    private String thumbnailUrl;
    private String author;
    private String authorProfilePic;
    private String title;
    private int views;
    private String uploadTime;
    private String category;
    private String authorId;  // Changed to Author object
    private String videoUrl;
    private int likes;

    public int getLikes() {
        return likes;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
    // private Author authorId;  // Changed from String to Author object

    public static class Author {
        private String _id;
        private String username;
        private String profilePic;

        public String get_id() {
            return _id;
        }

        public String getUsername() {
            return username;
        }

        public String getProfilePic() {
            return profilePic;
        }
    }

    public String getId() {
        return id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorId() {
        return authorId;
    }
//    public Author getAuthorId() {
//        return authorId;
//    }

    public String getAuthorProfilePic() {
        return authorProfilePic;
    }

    public String getTitle() {
        return title;
    }

    public int getViews() {
        return views;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public String getCategory() {
        return category;
    }
}