import {
    getVideoModel, createVideoModel, getVideosModel, updateVideoModel, deleteVideoModel,
    getVideosWithAuthorDetails, getMixedVideos, getVideosByCategory,
    unlikeVideo, getVideosbyUserId, isUserLikedVideo, incrementVideoViews, getVideosByUsernameService
, getCommentsByVideoId, getCommentsByVideoIdServer} from '../services/videoPlay.js';
//import { getCommentsByVideoId, countCommentsByVideoId } from '../services/comments.js';
import { LikeVideo as toggleLikeVideo } from '../services/videoPlay.js';



export async function getVideos(req, res) {
    try {
        const videos = await getVideosWithAuthorDetails();
        res.render('allVideos', { videos });
    } catch (error) {
        console.error('Error fetching videos with author details:', error);
        res.status(500).send('Failed to retrieve videos');
    }
}

export async function getVideo(req, res) {
    try {
        const video = await getVideoModel(req.params.pid);
        if (video) {
            res.status(200).json(video);
        } else {
            res.status(404).send('Video not found');
        }
    } catch (error) {
        res.status(500).send('Error retrieving video');
    }
}

export async function createVideo(req, res) {
    console.log("createVideo");
    console.log("the details are", req.body);

    try {
        const videoData = {
            ...req.body,
            videoUrl: req.files.video ? `/media/${req.files.video[0].filename}` : '',
            thumbnailUrl: req.files.thumbnail ? `/media/${req.files.thumbnail[0].filename}` : '',
        };
        await createVideoModel(videoData);
        res.json({ message: 'Video created successfully', data: videoData });
    } catch (error) {
        res.status(500).send('Failed to create video');
    }
}


export async function updateVideo(req, res) {
    console.log("updateVideo");

    try {
        const updateData = {
            ...req.body,
            videoUrl: req.files.video ? `/media/${req.files.video[0].filename}` : undefined,
            thumbnailUrl: req.files.thumbnail ? `/media/${req.files.thumbnail[0].filename}` : undefined,
        };

        const updatedVideo = await updateVideoModel(req.params.pid, updateData);

        if (updatedVideo) {
            res.json({ message: 'Video updated successfully', data: updatedVideo });
        } else {
            res.status(404).send('Video not found');
        }
    } catch (error) {
        res.status(500).send('Failed to update video');
    }
}



export async function deleteVideo(req, res) {
    console.log("deleteVideo");
    try {
        const deletedVideo = await deleteVideoModel(req.params.pid);
        if (deletedVideo) {
            res.send('Video deleted successfully');
        } else {
            res.status(404).send('Video not found');
        }
    } catch (error) {
        res.status(500).send('Failed to delete video');
    }
}

export async function fetchComments(req, res) {
    try {
        const videoId = req.params.videoId;  // Get video ID from request parameters
        const comments = await getCommentsByVideoId(videoId);
        res.json(comments);
    } catch (error) {
        res.status(500).send(error.message);
    }
}

export async function fetchCommentCount(req, res) {
    try {
        const videoId = req.params.videoId;  // Get video ID from request parameters
        const count = await countCommentsByVideoId(videoId);
        res.json({ count });
    } catch (error) {
        res.status(500).send(error.message);
    }
}

export async function fetchMixedVideos(req, res) {
    console.log("fetchMixedVideos");
    try {
        const videos = await getMixedVideos();
        res.json(videos);
    } catch (error) {
        res.status(500).send({ message: "Error fetching videos", error: error.message });
    }
}

export async function fetchVideosByCategory(req, res) {
    try {
        const category = req.params.category;  
        const videos = await getVideosByCategory(category);
        res.json(videos);
    } catch (error) {
        res.status(500).send({ message: "Error fetching videos by category", error: error.message });
    }
}

export const likeVideo = async (req, res) => {
    const { pid: videoId } = req.params;
    const userId = req.user.id; 

    try {
        const video = await toggleLikeVideo(videoId, userId);
        res.status(200).json(video);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

//unlike video to unlikeVideo in services/videoPlay.js
export const UnlikeVideo = async (req, res) => {
    const { pid: videoId } = req.params;
    const userId = req.user.id; 

    try {
        // Call the service function to toggle unlike the video
        const video = await unlikeVideo(videoId, userId, false);
        res.status(200).json(video);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

//deal getVideosByUserId in services/videoPlay.js
export async function getVideosByUserId(req, res) {
    //console.log("getVideosByUserId");
    try {
        const userId = req.params.id;
        const videos = await getVideosbyUserId(userId);
        res.json(videos);
    } catch (error) {
        res.status(500).send(error.message);
    }
}
//deal getVideosByUsername in services/videoPlay.js
export async function getVideosByUsername(req, res) {
    try {
        const username = req.params.username;
        const videos = await getVideosByUsernameService(username);
        res.json(videos);
    } catch (error) {
        res.status(500).send(error.message);
    }
}

//deal isUserLikedVideo in services/videoPlay.js
export async function getUserLikedVideo(req, res) {
    const videoId = req.params.pid;
    const userId = req.user._id; 
    //if it is a guest, then isLiked will be false
    if (!userId) {
        return res.json({ isLiked: false });
    }

    try {
        const isLiked = await isUserLikedVideo(videoId, userId);
        res.json({ isLiked });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
}


export async function addView(req, res) {
    const { pid } = req.params;  

    try {
        const updatedVideo = await incrementVideoViews(pid);
        if (updatedVideo) {
            res.status(200).json({ message: 'View added successfully', views: updatedVideo.views });
        } else {
            res.status(404).send('Video not found');
        }
    } catch (error) {
        res.status(500).send('Failed to increment views');
    }
}

export async function getWatchPageData(req, res) {
    try {
        const videoId = req.params.pid;
        const userId = req.params.id;

        // Get video details
        const video = await getVideoModel(videoId);
        if (!video) {
            return res.status(404).json({ message: 'Video not found' });
        }
        // Increment views
        const updatedVideo = await incrementVideoViews(videoId);

        // Get comments
        const comments = await getCommentsByVideoId(videoId);
        // Get comment count
        const commentCount = await countCommentsByVideoId(videoId);
        res.status(200).json({
            video: updatedVideo,
            comments: comments,
            commentCount: commentCount
        });
    } catch (error) {
        console.error('Failed to fetch watch page data:', error);
        res.status(500).json({ message: 'Failed to fetch watch page data' });
    }
}

export async function getVideoComments(req, res) {
    try {
        const videoId = req.params.videoId;
        const comments = await getCommentsByVideoIdServer(videoId);
        res.json(comments);
    } catch (error) {
        res.status(500).send(error.message);
    }
}

