import express from 'express'

const router = express.Router();

import {
    addComment, deleteComment, likeComment, unlikeComment, updateComment,
    getUserLikedComment, getUserLikes, resetGuestLikes
} from '../controllers/comments.js';

import { isLoggedIn } from '../middlewares/auth.js';


///get comments of a video and count is on routes/videoPlay.js///

//////actions on comments/////

//a route to add a comment on video
router.post('/users/:id/videos/:pid/comments', isLoggedIn, addComment);

//a route to update a comment of videos
router.put('/users/:id/videos/:pid/comments/:cid', isLoggedIn, updateComment);

//a route to delete a comment of videos
router.delete('/users/:id/videos/:pid/comments/:cid', isLoggedIn, deleteComment);

//get ahead if a user liked the comment or not (return false on guest)
router.get('/users/:id/videos/:pid/comments/:cid', isLoggedIn, getUserLikedComment);

//a route to like a comment of videos
router.put('/users/:id/videos/:pid/comments/:cid/like', isLoggedIn, likeComment);

//a route to unlike a comment of videos
router.put('/users/:id/videos/:pid/comments/:cid/unLike', isLoggedIn, unlikeComment);

// Route to get likes for a logged-in user
router.get('/comments/likes/:userId', isLoggedIn, getUserLikes);


export default router;
