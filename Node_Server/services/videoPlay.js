import Video from '../models/videoPlay.js'; // Import the Mongoose model
import Comment from '../models/comments.js';
import User from '../models/users.js';

// Fetch all!!!! videos from the database - just in case we need it
// we use mixed videos funciton down the page
export async function getVideosModel() {
    return await Video.find().populate('authorId', 'username profilePic');
}

// Function to get a video by its ID and populate the author details
export async function getVideoModel(id) {
    // console.log(id);
    try {

        // Fetch the video by its ID and populate the author details
        const video = await Video.findById(id)
            .populate('authorId', 'username profilePic'); // Populate author details

        if (!video) {
            return null; // or throw an error if you prefer
        }

        //add 1 to the views of the video by int updated Views
        let updatedViews = video.views;
        updatedViews = updatedViews + 1;
        const debugViews = incrementVideoViews(id);

        // Fetch the comments for the video
        const commentsPromise = getCommentsByVideoId(id);
        const [commentsList] = await Promise.all([commentsPromise]);

        // Transform the video object to include authorProfilePic
        const transformedVideo = {
            _id: video._id,
            thumbnailUrl: video.thumbnailUrl,
            title: video.title,
            author: video.authorId.username,
            authorId: video.authorId._id,
            authorName: video.authorId.username, // Use the populated username
            authorProfilePic: video.authorId.profilePic, // Add the profile picture
            views: updatedViews,
            uploadTime: video.uploadTime,
            videoUrl: video.videoUrl,
            category: video.category,
            likes: video.likes,
            likedBy: video.likedBy,
            commentsIds: video.comments, //the include comments id list in video model
            commentsList: commentsList, //the list of comments
            __v: video.__v
        };

        return transformedVideo;
    } catch (error) {
        console.error('Error fetching video:', error);
        throw error;
    }
}

// Function to get comments by video ID
export async function getCommentsByVideoId(videoId) {
    try {
        return await Comment.find({ videoId }).populate('userId', 'username profilePic');
    } catch (error) {
        console.error('Error retrieving comments:', error.message);
        return [];  // Return an empty array if there's an error
    }
}

export async function getCommentsByVideoIdServer(videoId) {
    try {
        const comments = await Comment.find({ videoId }).populate('userId', 'username profilePic');
        return comments.map(comment => ({
            _id: comment._id,
            likes: comment.likes,
            text: comment.text,
            uploadTime: comment.uploadTime,
            username: comment.userId.username,
            profilePicUrl: comment.userId.profilePic
        }));
    } catch (error) {
        console.error('Error retrieving comments:', error.message);
        return [];
    }
}


export async function createVideoModel(videoData) {
    if (!videoData.uploadTime) {
        videoData.uploadTime = formatDate(new Date()); // format is in the bottom of the page
    }
    const video = new Video(videoData); // Create a new video instance with the passed data
    console.log('the video is:', video);
    return await video.save(); // Save the new video to the database
}

export async function updateVideoModel(id, updateData) {
    return await Video.findByIdAndUpdate(id, updateData, { new: true }); // Returns the updated document
}

export async function deleteVideoModel(id) {
    return await Video.findByIdAndDelete(id); // Deletes the video by its ID
}

export async function LikeVideo(videoId, userId) {
    return await Video.findByIdAndUpdate(
        videoId,
        {
            $inc: { likes: 1 },
            $addToSet: { likedBy: userId }  // Ensures the user ID is only added once
        },
        { new: true }
    );
}

export async function unlikeVideo(videoId, userId) {
    return await Video.findByIdAndUpdate(
        videoId,
        {
            $inc: { likes: -1 },
            $pull: { likedBy: userId }  // Removes the user ID from the array
        },
        { new: true }
    );
}


export async function isUserLikedVideo(videoId, userId) {
    const video = await Video.findById(videoId);
    if (!video) {
        throw new Error('Video not found');
    }
    return video.likedBy.includes(userId);
}

// Function to get videos with editable author details that showed - username and profilePic
export async function getVideosWithAuthorDetails() {
    return await Video.find()
        .populate({
            path: 'authorId',
            select: 'username profilePic -_id'  // Only fetch the username and profilePic
        });
}

// Function to check if a user is the author of a video
export async function isUserTheAuthor(videoId, userId) {
    const video = await Video.findById(videoId);
    if (!video) {
        throw new Error('Video not found');
    }
    return video.authorId.toString() === userId;
}


// Function to get all videos by a specific user and populate the author details
export async function getVideosbyUserId(userId) {
    try {
        const userVideos = await Video.find({ authorId: userId })
            .populate('authorId', 'username profilePic')
            .select('thumbnailUrl title views uploadTime category videoUrl');

        // Check if userVideos is an array and not empty
        if (Array.isArray(userVideos) && userVideos.length > 0) {
            // Transform the result to include only the required fields
            const transformedVideos = userVideos.map(video => ({
                _id: video._id,
                thumbnailUrl: video.thumbnailUrl,
                author: video.authorId.username,
                authorId: video.authorId._id,
                authorProfilePic: video.authorId.profilePic,
                title: video.title,
                views: video.views,
                uploadTime: video.uploadTime,
                category: video.category,
                videoUrl: video.videoUrl
            }));
            return transformedVideos;
        } else {
            return []; // Return empty array if no videos found or userVideos is not array


        }
    } catch (error) {
        console.error('Failed to fetch videos by user ID:', error);
        throw error;
    }
}

// get videos by username
export async function getVideosByUsernameService(username) {
    try {
        const user = await User.findOne({ username: username });
        if (!user) {
            return [];
        }
        return await getVideosbyUserId(user._id);
    } catch (error) {
        console.error('Failed to fetch videos by username:', error);
        throw error;
    }
}

export async function getMixedVideos() {
    try {
        // Fetch the top 10 viewed videos with selected fields
        const topVideos = await Video.find()
            .sort({ views: -1 })
            .limit(10)
            .populate('authorId', 'username profilePic')
            .select('thumbnailUrl title views uploadTime category videoUrl');

        // Get IDs of topVideos to exclude them from the random selection
        const topVideoIds = topVideos.map(video => video._id);

        // Fetch random videos excluding the top viewed ones
        const totalVideosCount = await Video.countDocuments();
        const randomVideosCount = Math.min(10, totalVideosCount - topVideos.length);
        const randomVideos = await Video.aggregate([
            { $match: { _id: { $nin: topVideoIds } } },
            { $sample: { size: randomVideosCount } },
            { $project: { thumbnailUrl: 1, title: 1, views: 1, uploadTime: 1, category: 1,
                videoUrl: 1 } }
        ]);

        // Populate author details for random videos
        const randomVideoIds = randomVideos.map(video => video._id);
        const populatedRandomVideos = await Video.find({ _id: { $in: randomVideoIds } })
            .populate('authorId', 'username profilePic')
            .select('thumbnailUrl title views uploadTime category videoUrl');

        // Combine and shuffle the array
        const combinedVideos = [...topVideos, ...populatedRandomVideos];
        for (let i = combinedVideos.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [combinedVideos[i], combinedVideos[j]] = [combinedVideos[j], combinedVideos[i]];
        }
        // console.log(combinedVideos);

        // Transform the result to include only the required fields
        const transformedVideos = combinedVideos.map(video => ({
            _id: video._id,
            thumbnailUrl: video.thumbnailUrl,
            author: video.authorId.username,
            authorId: video.authorId._id,
            authorProfilePic: video.authorId.profilePic,
            title: video.title,
            views: video.views,
            uploadTime: video.uploadTime,
            category: video.category,
            videoUrl: video.videoUrl
        }));

        return transformedVideos;
    } catch (error) {
        console.error('Failed to fetch mixed videos:', error);
        throw error;
    }
}

// Function to get all videos by a specific category and populate the author details
export async function getVideosByCategory(category) {
    try {
        const categoryVideos = await Video.find({ category })
            .populate('authorId', 'username profilePic')
            .select('thumbnailUrl title views uploadTime');

        // Check if categoryVideos is an array and not empty
        if (Array.isArray(categoryVideos) && categoryVideos.length > 0) {
            // Transform the result to include only the required fields
            const transformedVideos = categoryVideos.map(video => ({
                _id: video._id,
                thumbnailUrl: video.thumbnailUrl,
                author: video.authorId.username,
                authorId: video.authorId._id,
                authorProfilePic: video.authorId.profilePic,
                title: video.title,
                views: video.views,
                uploadTime: video.uploadTime
            }));
            return transformedVideos;
        } else {
            return []; // Return empty array if no videos found or categoryVideos is not array
        }
    } catch (error) {
        console.error('Failed to fetch videos by category:', error);
        throw error;
    }
}


export async function incrementVideoViews(videoId) {
    try {
        return await Video.findByIdAndUpdate(
            videoId,
            { $inc: { views: 1 } },
            { new: true }  // This option returns the document after the update
        );
    } catch (error) {
        console.error('Failed to increment video views:', error);
        throw error;  // Rethrow the error for handling at a higher level
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