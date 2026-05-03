// services/comments.js
import Comment from '../models/comments.js';
import Video from '../models/videoPlay.js';
//import mongoose from 'mongoose';
import mongoose from 'mongoose';


export async function createCommentModel(commentData) {
    console.log('commentData:', commentData);
    try {
        //add uploadTime to commentData
        if (!commentData.uploadTime) {
            commentData.uploadTime = formatDate(new Date());
        }
        console.log('commentData:', commentData);
        // Create and save the new comment
        const newComment = new Comment(commentData);
        console.log('newComment:', newComment);
        const savedComment = await newComment.save();

        // Push the saved comment's ID to the corresponding video's comments array
        const updatedVideo = await Video.findByIdAndUpdate(
            commentData.videoId,
            { $push: { comments: savedComment._id } },
            { new: true }
        );

        if (!updatedVideo) {
            // If the video couldn't be updated, rollback the comment creation
            await Comment.findByIdAndDelete(savedComment._id);
            throw new Error('Video not found; Comment was not added');
        }

        return savedComment;
    } catch (error) {
        console.error('Error creating comment:', error.message);
        throw new Error('Error creating comment: ' + error.message);
    }
}


export async function editCommentModel(commentId, newText) {
    try {
        const updatedComment = await Comment.findByIdAndUpdate(
            commentId,
            { $set: { text: newText } },
            { new: true }
        );
        if (!updatedComment) {
            throw new Error('Comment not found');
        }
        return updatedComment;
    } catch (error) {
        throw new Error('Error updating comment: ' + error.message);
    }
}


export async function deleteCommentModel(commentId) {
    try {
        const comment = await Comment.findById(commentId);
        if (!comment) {
            throw new Error('Comment not found');
        }

        // Delete the comment
        await Comment.findByIdAndDelete(commentId);

        // Pull the comment ID from the corresponding video's comments array
        await Video.findByIdAndUpdate(
            comment.videoId,
            { $pull: { comments: comment._id } }
        );

        return { message: 'Comment deleted successfully' };
    } catch (error) {
        throw new Error('Error deleting comment: ' + error.message);
    }
}

export async function isUserTheAuthorOfComment(commentId, userId) {
    try {
        const comment = await Comment.findById(commentId);
        if (!comment) {
            throw new Error('Comment not found');
        }
        return comment.userId.toString() === userId;
    } catch (error) {
        throw new Error('Error checking authorship of comment: ' + error.message);
    }
}


export async function LikeComment(commentId, userId) {
    try {
        const updatedComment = await Comment.findByIdAndUpdate(
            commentId,
            {
                $inc: { likes: 1 },
                $addToSet: { likedByUsers: userId }  // Ensures the user ID is only added once
            },
            { new: true }
        );
        if (!updatedComment) {
            throw new Error('Comment not found');
        }
        return updatedComment;
    } catch (error) {
        throw new Error('Error liking comment: ' + error.message);
    }
}

export async function UnlikeComment(commentId, userId) {
    try {
        const updatedComment = await Comment.findByIdAndUpdate(
            commentId,
            {
                $inc: { likes: -1 },
                $pull: { likedByUsers: userId }  // Removes the user ID from the array
            },
            { new: true }
        );
        if (!updatedComment) {
            throw new Error('Comment not found');
        }
        return updatedComment;
    } catch (error) {
        throw new Error('Error unliking comment: ' + error.message);
    }
}

export async function isUserLikedComment(commentId, userId) {
    try {
        const comment = await Comment.findById(commentId);
        if (!comment) {
            throw new Error('Comment not found');
        }
        // Check if the user's ID is in the likedByUsers array
        return comment.likedByUsers.includes(userId);
    } catch (error) {
        throw new Error('Error checking if user liked comment: ' + error.message);
    }
}


function formatDate(date) {
    let hours = date.getHours();
    let minutes = date.getMinutes();
    let day = date.getDate();
    let month = date.getMonth() + 1; // JavaScript months are zero-based
    let year = date.getFullYear().toString().slice(-2); // Get last two digits of year

    // Ensure two digits by adding leading zeros if necessary
    hours = hours < 10 ? '0' + hours : hours;
    minutes = minutes < 10 ? '0' + minutes : minutes;
    day = day < 10 ? '0' + day : day;
    month = month < 10 ? '0' + month : month;

    return `${hours}:${minutes} ${day}/${month}/${year}`;
}

//old code for get comments by video id not in use//
export async function oldgetCommentsByVideoId(videoId) {
    try {
        const video = await Video.findById(videoId).populate({
            path: 'comments',
            populate: { path: 'userId', select: 'username profilePic' }  // Optionally populate user details
        });
        if (!video) {
            throw new Error('Video not found');
        }
        return video.comments;
    } catch (error) {
        throw new Error('Error retrieving comments: ' + error.message);
    }
}
//end of old code for get comments by video id not in use//

//old code for get comments by video id not in use
export async function countCommentsByVideoId(videoId) {
    try {
        const video = await Video.findById(videoId).populate('comments');
        return video ? video.comments.length : 0;
    } catch (error) {
        console.error('Error counting comments:', error.message);
        return 0;  // Return zero if there's an error
    }
}
//end of old code for get comments by video id not in use //


export async function fetchUserLikes(userId) {
    try {
        const comments = await Comment.find({ likedByUsers: userId }).select('_id');
        return comments.map(comment => comment._id);
    } catch (error) {
        throw new Error('Error retrieving user likes: ' + error.message);
    }
}

export async function resetLikesForGuest() {
    // This would actually be handled client-side since guest views don't save state
    // Returning a simulated empty array for demonstration purposes
    return [];
}




export default Comment;