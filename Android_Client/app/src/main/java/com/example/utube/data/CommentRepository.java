package com.example.utube.data;

import android.app.Application;
import com.example.utube.models.CommentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentRepository {
    private CommentDao commentDao;
    private ExecutorService executorService;

    public CommentRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        commentDao = db.commentDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public long insert(CommentEntity comment) {
        try {
            return executorService.submit(() -> commentDao.insert(comment)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<CommentEntity> getCommentsForVideo(String videoId) {
        try {
            return executorService.submit(() -> commentDao.getCommentsForVideo(videoId)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateComment(CommentEntity comment) {
        executorService.execute(() -> commentDao.updateComment(comment));
    }

    public void deleteComment(CommentEntity comment) {
        executorService.execute(() -> commentDao.deleteComment(comment));
    }

    public CommentEntity getCommentById(int commentId) {
        try {
            return executorService.submit(() -> commentDao.getCommentById(commentId)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteAllCommentsByUsername(String username) {
        executorService.execute(() -> commentDao.deleteAllCommentsByUsername(username));
    }

    public void mergeCommentsForVideo(String videoId, List<CommentEntity> newComments) {
        executorService.execute(() -> {
            List<String> newServerIds = new ArrayList<>();

            for (CommentEntity newComment : newComments) {
                newServerIds.add(newComment.serverId);
                CommentEntity existingComment = commentDao.findCommentByServerIdAndVideoId(newComment.serverId, videoId);
                if (existingComment != null) {
                    existingComment.text = newComment.text;
                    existingComment.likes = newComment.likes;
                    existingComment.uploadTime = newComment.uploadTime;
                    // Update other fields as necessary
                    commentDao.updateComment(existingComment);
                } else {
                    commentDao.insert(newComment);
                }
            }

            commentDao.deleteCommentsNotInList(videoId, newServerIds);
        });
    }


}