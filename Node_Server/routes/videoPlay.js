import express from 'express';
import { upload } from './mediaRoutes.js';
import {
  getVideos, getVideo, createVideo, fetchComments, fetchCommentCount, fetchMixedVideos, fetchVideosByCategory
  , updateVideo, deleteVideo, likeVideo, UnlikeVideo, getVideosByUserId,
  getUserLikedVideo, addView, getWatchPageData, getVideosByUsername, getVideoComments
} from '../controllers/videoPlay.js'
//import { isLoggedIn } from '../controllers/tokens.js';
import { addComment, deleteComment, likeComment, unlikeComment, updateComment } from '../controllers/comments.js';
import { isUserLikedVideo } from '../services/videoPlay.js';
//import { isLoggedIn} from '../controllers/login.js'
import { isLoggedIn } from '../middlewares/auth.js';
const router = express.Router();


//////videos list page/////

// Route to get a mix of 20 videos (or less if there are less than 20) from the database
router.get('/videos', fetchMixedVideos);

//get user videos by id
router.get('/users/:id/videos', getVideosByUserId);

// get user videos by username
router.get('/users/name/:username/videos', getVideosByUsername)

// Route to get videos by category (can implement also on client side, from the videos list page he got)
// need to update the real adress, depand on how the button will be on client side//
router.get('/videos/category/:category', fetchVideosByCategory);

//search videos is on client side (search bar), from the videos list page he got

///actions on videos////

// Route to upload a new video
router.post('/users/:id/videos', isLoggedIn, upload.fields([{ name: 'video', maxCount: 1 }, { name: 'thumbnail', maxCount: 1 }]), createVideo);

//update video
router.put('/users/:id/videos/:pid', isLoggedIn, upload.fields([{ name: 'video', maxCount: 1 }, { name: 'thumbnail', maxCount: 1 }]), updateVideo);

//delete video
// router.delete('/users/:id/videos/:pid', isLoggedIn, deleteVideo);
router.delete('/users/:id/videos/:pid', isLoggedIn, deleteVideo);


//////watch video page/////

//get a video to watch
router.get('/users/:id/videos/:pid', getVideo);

///likes on videos////

//get ahead if a user liked the video or not (return false on guest)
router.get('/users/:id/videos/:pid', isLoggedIn, getUserLikedVideo);

//a route to like a video
router.put('/users/:id/videos/:pid/likes', isLoggedIn, likeVideo);

//a route to unlike a video
router.put('/users/:id/videos/:pid/unlikes', isLoggedIn, UnlikeVideo);

//a route to get video comments
router.get('/videos/:videoId/comments', getVideoComments);


export default router;







