package com.example.utube.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.utube.models.VideoEntity;

import java.util.List;


@Dao
public interface VideoDao {

    //@Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VideoEntity video);

    @Query("SELECT * FROM videos")
    List<VideoEntity> getAllVideos();

    @Query("SELECT * FROM videos WHERE author = :author")
    List<VideoEntity> getVideosForAuthor(String author);

    @Update
    void updateVideo(VideoEntity video);

    @Delete
    void deleteVideo(VideoEntity video);

    @Query("SELECT * FROM videos WHERE id = :id")
    VideoEntity getVideoById(String id);

    @Query("UPDATE videos SET views = views + 1 WHERE id = :videoId")
    void incrementViews(String videoId);

    @Query("SELECT * FROM videos")
    LiveData<List<VideoEntity>> getAllVideosLive();

    @Query("SELECT COUNT(*) FROM videos")
    int getCount();

    @Query("DELETE FROM videos WHERE author = :author")
    void deleteAllVideosByAuthor(String author);
}