package com.example.utube.data;

import androidx.room.*;
import com.example.utube.models.CommentEntity;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    long insert(CommentEntity comment);

    @Query("SELECT * FROM comments WHERE videoId = :videoId")
    List<CommentEntity> getCommentsForVideo(String videoId);

    @Update
    void updateComment(CommentEntity comment);

    @Delete
    void deleteComment(CommentEntity comment);
    @Query("SELECT * FROM comments WHERE id = :commentId")
    CommentEntity getCommentById(int commentId);

    @Query("DELETE FROM comments WHERE username = :username")
    void deleteAllCommentsByUsername(String username);

    @Query("SELECT * FROM comments WHERE videoId = :videoId AND server_id = :serverId LIMIT 1")
    CommentEntity findCommentByServerIdAndVideoId(String serverId, String videoId);

    @Query("SELECT server_id FROM comments WHERE videoId = :videoId")
    List<String> getServerIdsForVideo(String videoId);

    @Query("DELETE FROM comments WHERE videoId = :videoId AND server_id NOT IN (:serverIds)")
    void deleteCommentsNotInList(String videoId, List<String> serverIds);
}