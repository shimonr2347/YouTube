import express from 'express';
import bodyParser from 'body-parser';
import cors from 'cors';
import fs from 'fs';
import mongoose from 'mongoose';
import customEnv from 'custom-env';
//add dotenv for environment variables
import dotenv from 'dotenv';
// mediaRoutes is for uploading and replacing media files
import mediaRoutes from './routes/mediaRoutes.js';
import routerVideoPlay from './routes/videoPlay.js';
import routerToken from './routes/tokens.js';
import userRouter from './routes/users.js';
import routerComments from './routes/comments.js';

import User from './models/users.js';
import Video from './models/videoPlay.js';
import {
    createVideoModel, updateVideoModel, deleteVideoModel, unlikeVideo, isUserLikedVideo,
    isUserTheAuthor, getVideosbyUserId
} from './services/videoPlay.js';
import { updateUserModel, deleteUserModel } from './services/users.js';
import {
    createCommentModel, editCommentModel, deleteCommentModel, isUserTheAuthorOfComment,
    LikeComment, UnlikeComment, isUserLikedComment, countCommentsByVideoId
} from './services/comments.js';
import { fetchMixedVideos, fetchVideosByCategory } from './controllers/videoPlay.js';
import request from 'supertest';  // npm install supertest --save-dev
import cppServerRouter from './routes/cppServer.js';
import http from 'http';
import { createThreadForUser, closeThreadForUser, sendWatchNotification } from './services/cppServerService.js';
import { getVideoRecommendations } from './controllers/cppServerController.js';


dotenv.config();

// Environment variables
customEnv.env(process.env.NODE_ENV || 'local', './config');

const server = express();

// Middleware setup
server.use(cors());
server.use(express.static('public'));
server.use(bodyParser.urlencoded({ extended: true }));
server.use(express.json({ limit: '10mb' }));  // Keep this if you anticipate large JSON payloads, otherwise it's safe to remove

// Route configuration
server.use('/api', userRouter);
server.use('/api', routerToken);
server.use('/api', routerVideoPlay);
server.use('/api', mediaRoutes);  // Dedicated endpoint for media operations
server.use('/api', routerComments);

server.use('/api/cpp', cppServerRouter);

(async () => {
    // MongoDB connection
    mongoose.connect(process.env.CONNECTION_STRING)
        .then(() => {
            console.log('MongoDB connected');
            checkAndLoadData();  // check if the mongoDB is empty and load the data
            //use test functions here, they are commented out below
        })
        .catch(err => console.error('MongoDB connection error:', err));
})()

// Load initial data if no data existsx  in MongoDB
async function checkAndLoadData() {
    const userExists = await User.findOne();
    const videoExists = await Video.findOne();

    if (!userExists && !videoExists) {
        console.log("No data found in MongoDB, loading initial data...");
        loadData();
    } else {
        console.log("Data already exists in MongoDB, skipping initial load.");
    }
}

async function loadData() {
    try {
        const usersData = JSON.parse(fs.readFileSync('./users.json', 'utf8'));
        const videosData = JSON.parse(fs.readFileSync('./videos.json', 'utf8'));

        await User.deleteMany({});
        const createdUsers = await User.insertMany(usersData);

        // Create a map of usernames to user IDs
        const usernameToIdMap = createdUsers.reduce((map, user) => {
            map[user.username] = user._id;
            return map;
        }, {});

        // Transform video data to include authorId instead of author username
        const transformedVideos = videosData.map(video => ({
            ...video,
            authorId: usernameToIdMap[video.author],  // Map the username to ObjectId
            authorName: video.author,  // Optionally keep authorName if needed
        }));

        await Video.deleteMany({});
        await Video.insertMany(transformedVideos);
        console.log('Data loaded successfully');
    } catch (err) {
        console.error('Failed to load data:', err);
    }
}


// Start the server
const PORT = process.env.PORT || 89;
server.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});





/////////////////////tests/////////////////////

/*
async function testRecommendationSystem() {
    await createViewerHistory();
    console.log("Starting recommendation system test...");

    // Simulate a request object
    const req = {
        body: {
            videoId: "66990ce8d7e807e5f0008505",
            token: "guest"
        }
    };

    // Simulate a response object
    const res = {
        status: function (statusCode) {
            console.log(`Response Status: ${statusCode}`);
            return this;
        },
        json: function (data) {
            console.log("Recommendation Response:");
            console.log(JSON.stringify(data, null, 2));
        }
    };

    try {
        await getVideoRecommendations(req, res);
        console.log("Recommendation test completed.");
    } catch (error) {
        console.error("Error in recommendation test:", error);
    }
}

async function createViewerHistory() {
    const videoIds = [
        "66990ce8d7e807e5f0008505",
        "6699457ad7e807e5f00089da",
        "66994733d7e807e5f0008a23",
        "669948b8d7e807e5f0008a6f",
        "66990ce8d7e807e5f000850d",
        "66990ce8d7e807e5f000850e"
    ];
    const userIds = [
        "66990ce8d7e807e5f00084ff",
        "66990ce8d7e807e5f0008500",
        "66990ce8d7e807e5f0008502"
    ];

    console.log("Creating viewer history...");

    for (let i = 0; i < videoIds.length; i++) {
        for (let j = 0; j < userIds.length; j++) {
            if (Math.random() < 0.7) {  // 70% chance of a user watching each video
                await sendWatchNotification(userIds[j], videoIds[i]);
                console.log(`User ${userIds[j]} watched video ${videoIds[i]}`);
            }
        }
    }

    console.log("Viewer history created.");
} */
// async function testCppServerInteractions() {
//     const users = [
//         { id: 'user1', name: 'Alice' },
//         { id: 'user2', name: 'Bob' }
//     ];
//     const videos = ['video1', 'video2', 'video3'];

//     const threadIds = {};

//     try {
//         // Create threads for users
//         for (const user of users) {
//             console.log(`Creating thread for ${user.name}...`);
//             const createResult = await createThreadForUser(user.id);
//             console.log(`Thread creation result for ${user.name}:`, createResult);

//             if (createResult.threadId) {
//                 threadIds[user.id] = createResult.threadId;
//                 console.log(`Thread ID for ${user.name}: ${threadIds[user.id]}`);
//             } else {
//                 throw new Error(`Failed to get thread ID for ${user.name}`);
//             }
//         }

//         // Simulate video watches
//         for (const user of users) {
//             for (const video of videos) {
//                 console.log(`${user.name} is watching ${video}...`);
//                 const watchResult = await sendWatchNotification(user.id, video);
//                 console.log(`Watch notification result for ${user.name} on ${video}:`, watchResult);

//                 if (watchResult.threadId === threadIds[user.id]) {
//                     console.log(`Correct thread used for ${user.name}`);
//                 } else {
//                     console.error(`Incorrect thread used for ${user.name}. Expected ${threadIds[user.id]}, got ${watchResult.threadId}`);
//                 }
//             }
//         }

//         // Close threads for users
//         for (const user of users) {
//             console.log(`Closing thread for ${user.name}...`);
//             const closeResult = await closeThreadForUser(user.id);
//             console.log(`Thread closing result for ${user.name}:`, closeResult);

//             if (closeResult.threadId === threadIds[user.id]) {
//                 console.log(`Correct thread closed for ${user.name}`);
//             } else {
//                 console.error(`Incorrect thread closure for ${user.name}. Expected ${threadIds[user.id]}, got ${closeResult.threadId}`);
//             }
//         }

//         console.log('All tests completed successfully!');
//     } catch (error) {
//         console.error('Error during C++ server interaction test:', error);
//     }
// }
/*async function testUserThreadLifecycle() {
    try {
        const userId = 'testUser123';  // Replace with a valid user ID from your system

        console.log('Creating thread...');
        const createResult = await createThreadForUser(userId);
        console.log('Thread creation result:', createResult);

        // Wait for a moment to simulate some user activity
        await new Promise(resolve => setTimeout(resolve, 2000));

        console.log('Closing thread...');
        const closeResult = await closeThreadForUser(userId);
        console.log('Thread closing result:', closeResult);
    } catch (error) {
        console.error('Error in user thread lifecycle test:', error);
    }
}*/
/*async function testCreateThreadForUser() {
    try {
        const userId = 'testUser123';  // Replace with a valid user ID from your system
        const result = await createThreadForUser(userId);
        console.log('Thread creation result:', result);
    } catch (error) {
        console.error('Error creating thread:', error);
    }
}*/
/*
function testCppServerCommunication() {
    const data = JSON.stringify({
        userId: 'testUser3',
        videoId: 'testVideo3'
    });

    const options = {
        hostname: 'localhost',
        port: 5555,
        path: '/',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': data.length
        },
        timeout: 5000 // 5 seconds timeout
    };

    const req = http.request(options, (res) => {
        let responseData = '';

        res.on('data', (chunk) => {
            responseData += chunk;
        });

        res.on('end', () => {
            console.log('C++ Server Notification Response:', responseData);
        });
    });

    req.on('error', (error) => {
        console.error('Error testing C++ server communication:', error);
        if (error.code === 'ECONNREFUSED') {
            console.error('Make sure the C++ server is running on port 5555');
        }
    });

    req.on('timeout', () => {
        console.error('Connection to C++ server timed out');
        req.abort();
    });

    req.write(data);
    req.end();
} */



/* Test the video creation to see date format
// Example Video Data for Testing
const testVideoData = {
    thumbnailUrl: "media/images_new/testThumbnail.jpg",
    title: "Test Video for Upload Time",
    authorId: "668167fb6349965e76c1be66",  // Use a valid MongoDB ObjectId string that exists in your database
    authorName: "Test Author",
    views: 0,
    videoUrl: "media/videos_new/testVideo.mp4",
    category: "Test",
    likes: 0,
    likedBy: [],
    comments: []
};

// Function to test video creation
async function testCreateVideo() {
    try {
        const newVideo = await createVideoModel(testVideoData);
        console.log('New Video Added:', newVideo);
    } catch (error) {
        console.error('Error creating video:', error);
    }
}
 */


// async function testUserFlow() {
//     // Simulate user registration
//     const registerResponse = await request(server)
//         .post('/api/SignUp')  // Adjusted endpoint with the /api prefix
//         .send({
//             username: "testuser",
//             password: "password123",
//             firstName: "Test",
//             lastName: "User",
//             email: "testuser@example.com"
//         });
//     console.log('Register Response:', registerResponse.body);

//     // Simulate user login
//     const loginResponse = await request(server)
//         .post('/api/login')  // Adjusted endpoint with the /api prefix
//         .send({
//             username: "testuser",
//             password: "password123"
//         });
//     const { token } = loginResponse.body;
//     console.log('Login Response:', loginResponse.body);

//     // Simulate liking a video with authentication
//     const likeResponse = await request(server)
//         .put(`/api/users/667ec704aa2855d236860a17/videos/667ec704aa2855d236860a1e/likes`)  // Adjusted endpoint with the /api prefix
//         .set('Authorization', `Bearer ${token}`);
//     console.log('Like Video Response:', likeResponse.body);

//     // Simulate trying to like a video as a guest (no token)
//     const guestLikeResponse = await request(server)
//         .put(`/api/users/667ec704aa2855d236860a17/videos/667ec704aa2855d236860a1e/likes`);  // Adjusted endpoint with the /api prefix
//     console.log('Guest Like Video Response:', guestLikeResponse.body);
// }



// Load initial data into MongoDB
// async function loadData() {
//     try {
//         // Read users and videos data from JSON files
//         const usersData = JSON.parse(fs.readFileSync('./users.json', 'utf8'));
//         const videosData = JSON.parse(fs.readFileSync('./videos.json', 'utf8'));

//         // Clear existing data and load new data
//         await User.deleteMany({});
//         await User.insertMany(usersData);
//         await Video.deleteMany({});
//         await Video.insertMany(videosData);
//     } catch (err) {
//         console.error('Failed to load data:', err);
//     }
// }


// // Function to test fetching videos by category
// async function testFetchVideosByCategory() {
//     const fakeReq = {
//         params: {
//             category: 'Sport'  // Change to the category you'd like to test
//         }
//     };
//     const fakeRes = {
//         json: (data) => console.log("Test Fetch Videos By Category:", data),
//         status: function (statusCode) {
//             console.log(`HTTP Status: ${statusCode}`);
//             return this;  // Allow method chaining
//         },
//         send: (data) => console.log(data)
//     };

//     try {
//         await fetchVideosByCategory(fakeReq, fakeRes);
//     } catch (err) {
//         console.error('Error during fetchVideosByCategory test:', err);
//     }
// }

// // Function to test fetching mixed videos
// async function testFetchMixedVideos() {
//     const fakeReq = {};  // Mock request object, add properties if your controller uses them
//     const fakeRes = {
//         json: (data) => console.log("Test Fetch Mixed Videos:", data),
//         status: function (statusCode) {
//             console.log(`HTTP Status: ${statusCode}`);
//             return this;  // Allow method chaining
//         },
//         send: (data) => console.log(data)
//     };

//     try {
//         await fetchMixedVideos(fakeReq, fakeRes);
//     } catch (err) {
//         console.error('Error during fetchMixedVideos test:', err);
//     }
// }


// async function testLogin() {
//     try {
//         const isValid = await checkUserNameAndPassword("Author 1", "Author 2");
//         console.log(`Login valid: ${isValid}`);
//     } catch (error) {
//         console.error('Error during login test:', error);
//     }
// }


// // test delete user
// async function testDeleteUser(userId) {
//     try {
//         console.log(`Attempting to delete user with ID: ${userId}`);
//         const user = await deleteUserModel(userId);
//         if (user) {
//             console.log(`User with ID: ${userId} was deleted successfully.`);
//         } else {
//             console.log(`No user found with ID: ${userId}.`);
//         }
//     } catch (error) {
//         console.error('Failed to delete user:', error);
//     }
// }

// async function testFetchComments() {
//     const videoId = '66771d56ee7de545aba5a4a1';  // Replace with an actual video ID from your database

//     try {    
//         console.log(`Fetching comments for video ID: ${videoId}`);
//         const comments = await getCommentsByVideoId(videoId);
//         console.log(`Comments for video ${videoId}:`, comments);
//         comments.forEach(comment => {
//             console.log(`- Comment text: "${comment.text}" by User ID: ${comment.userId}`);
//         });
//     } catch (error) {
//         console.error('Error fetching comments:', error);
//     }
// }

// async function testFetchCommentCount() {
//     const videoId = '66771d56ee7de545aba5a4a1';  // Replace with an actual video ID from your database

//     try {
//         console.log(`Fetching comment count for video ID: ${videoId}`);
//         const count = await countCommentsByVideoId(videoId);
//         console.log(`Total comments for video ${videoId}: ${count}`);
//     } catch (error) {
//         console.error('Error fetching comment count:', error);
//     }
// }


// async function testIsUserLikedComment() {
//     const commentId = "667722b7e4d39c07e840d0c7";  // Replace with an actual comment ID
//     const userId = "66771d56ee7de545aba5a49a";  // Replace with an actual user ID
//     const userId2 = "66771d56ee7de545aba5a49b";  // Replace with an actual user ID
//     try {
//         const hasLiked = await isUserLikedComment(commentId, userId2);
//         console.log(`Has user ${userId2} liked comment ${commentId}? ${hasLiked}`);
//         // hasLiked = await isUserLikedComment(commentId, userId2);
//         // console.log(`Has user ${userId2} liked comment ${commentId}? ${hasLiked}`);
//     } catch (error) {
//         console.error('Failed to check if user is the liker of the comment:', error);
//     }
// }

// async function testLikeAndUnlikeComment() {
//     const commentId = "667722b7e4d39c07e840d0c7";  // Replace with an actual comment ID
//     const userId = "66771d56ee7de545aba5a49a";  // Replace with an actual user ID

//     try {
//         console.log(`Liking comment ${commentId} by user ${userId}`);
//         const likedComment = await likeComment(commentId, userId);
//         console.log('Comment liked:', likedComment);

//         // console.log(`Unliking comment ${commentId} by user ${userId}`);
//         // const unlikedComment = await unlikeComment(commentId, userId);
//         // console.log('Comment unliked:', unlikedComment);
//     } catch (error) {
//         console.error('Failed to like or unlike comment:', error);
//     }
// }

// async function testIsUserTheAuthorOfComment() {
//     const commentId = "667722b7e4d39c07e840d0c7";  // Replace with an actual comment ID
//     const userId = "66771d56ee7de545aba5a49a";  // Replace with an actual user ID

//     try {
//         const isAuthor = await isUserTheAuthorOfComment(commentId, userId);
//         console.log(`1- Is user ${userId} the author of comment ${commentId}? ${isAuthor}`);
//     } catch (error) {
//         console.error('1 -Failed to check if user is the author of the comment:', error);
//     }
// }

// async function testIsUserTheAuthorOfComment2() {
//     const commentId = "667722b7e4d39c07e840d0c7";  // Replace with an actual comment ID
//     const userId = "66771d56ee7de545aba5a49c";  // Replace with an actual user ID

//     try {
//         const isAuthor = await isUserTheAuthorOfComment(commentId, userId);
//         console.log(`2 -Is user ${userId} the author of comment ${commentId}? ${isAuthor}`);
//     } catch (error) {
//         console.error('2 -Failed to check if user is the author of the comment:', error);
//     }
// }

// // Testing comment deletion
// async function testDeleteComment() {
//     const commentId = "66771ea4fef6b2921ffe42f6";  // Replace with an actual comment ID

//     try {
//         const result = await deleteCommentModel(commentId);
//         console.log(result.message);
//     } catch (error) {
//         console.error('Failed to delete comment:', error);
//     }
// }

// // Testing comment editing
// async function testEditComment() {
//     const commentId = "66771ea4fef6b2921ffe42f6";  // Replace with an actual comment ID
//     const newText = "Updated text for this comment.";

//     try {
//         const updatedComment = await editCommentModel(commentId, newText);
//         console.log('Comment updated:', updatedComment);
//     } catch (error) {
//         console.error('Failed to update comment:', error);
//     }
// }

// // Testing comment creation
// async function testCreateComment() {
//     const commentData = {
//         userId: "66782b4d5939f8d3739fc64f",  // Example user ID
//         videoId: "66782b4d5939f8d3739fc656",  // Example video ID
//         text: "Great video!",
//         uploadTime: new Date("2020-01-02T15:00:00Z"),
//         likes: 0,
//         likedByUsers: []
//     };

//     try {
//         const newComment = await createCommentModel(commentData);
//         console.log('Comment created and added to video:', newComment);
//     } catch (error) {
//         console.error('Failed to create comment:', error);
//     }
// }


// async function testDeleteUserAndVideos(userId) {
//     try {
//         console.log(`Checking videos before deletion for user ${userId}`);
//         const videosBefore = await getVideosByUserId(userId);
//         console.log(`Found ${videosBefore.length} videos before deletion.`);

//         const deletedUser = await deleteUserModel(userId);
//         if (deletedUser) {
//             console.log(`User deleted successfully. ID: ${userId}`);
//         }

//         console.log(`Checking videos after deletion for user ${userId}`);
//         const videosAfter = await getVideosByUserId(userId);
//         console.log(`Found ${videosAfter.length} videos after deletion.`);
//     } catch (error) {
//         console.error('Error in testDeleteUserAndVideos:', error);
//     }
// }



// async function testAuthorship() {
//     const videoId = "6676faa2de0663d0aaa2a23b";
//     const userId = "6676faa2de0663d0aaa2a234";

//     try {
//         const isAuthor = await isUserTheAuthor(videoId, userId);
//         console.log(`1 - Is user ${userId} the author of video ${videoId}? ${isAuthor}`);
//     } catch (error) {
//         console.error('Error checking authorship:', error);
//     }
// }

// async function testAuthorship2() {
//     const videoId = "6676faa2de0663d0aaa2a23b";
//     const userId = "6676faa2de0663d0aaa2a235";

//     try {
//         const isAuthor = await isUserTheAuthor(videoId, userId);
//         console.log(`2 - Is user ${userId} the author of video ${videoId}? ${isAuthor}`);
//     } catch (error) {
//         console.error('Error checking authorship:', error);
//     }
// }


// async function testUserLikedVideo() {
//     const videoId = '6676af630402f0c497e29d94';
//     const userId = '6676af630402f0c497e29d8c';

//     try {
//         const hasLiked = await isUserLikedVideo(videoId, userId);
//         console.log(`Has user ${userId} liked video ${videoId}?`, hasLiked);
//     } catch (error) {
//         console.error('Error checking if user liked video:', error);
//     }
// }

// async function testLikeAndUnlikeFeatures() {
//     const videoId = '6676af630402f0c497e29d93';
//     const userId = '6676af630402f0c497e29d8c';

//     try {
//         // const likedVideo = await likeVideo(videoId, userId);
//         // console.log('Video liked:', likedVideo);

//         const unlikedVideo = await unlikeVideo(videoId, userId);
//         console.log('Video unliked:', unlikedVideo);
//     } catch (error) {
//         console.error('Failed to like/unlike video:', error);
//     }
// }


// // Test user registration
// async function testRegisterUser() {
//     // Simulate request and response
//     const req = {
//         body: {
//             firstName: "Test",
//             lastName: "User",
//             date: "1990-01-01",
//             email: "testuser@example.com",
//             profilePic: "url_to_pic",
//             username: "testuser",
//             password: "password123",
//             passwordConfirm: "password123"
//         }
//     };
//     const res = {
//         status: (statusCode) => {
//             console.log(`Status Code: ${statusCode}`);
//             return {
//                 json: (data) => {
//                     console.log('Response:', data);
//                 },
//                 send: (data) => {
//                     console.log('Response:', data);
//                 }
//             };
//         }
//     };

//     await registerUser(req, res);
// }

// //test update and delete user manually
// async function updateAndDeleteSampleUser() {
//     const userId = '6676b7785c00cb8e630072f5';  // Use a valid user ID
//     try {
//         await updateUserModel(userId, { firstName: 'New Name' });  // Change attributes as needed
//         console.log('User updated');
//         await deleteUserModel(userId);
//         console.log('User deleted');
//     } catch (error) {
//         console.error('Error updating or deleting user:', error);
//     }
// }


// // test manually Add a sample video to the database to test the API
// async function addSampleVideo() {
//     const sampleVideoData = {
//         thumbnailUrl: "drawable/imvid23",
//         title: "tryAdd",
//         authorId: "6676f39196de4690f086aa54",
//         views: 0,
//         uploadTime: new Date(),  // Current date/time or specific date
//         videoUrl: "raw/vid23_oly08",
//         category: "News",
//         likes: 0,
//         comments: []  // Empty array if no comments
//     };

//     try {
//         const newVideo = await createVideoModel(sampleVideoData);
//         console.log('Sample video added:', newVideo);
//     } catch (error) {
//         console.error('Failed to add sample video:', error);
//     }
// }
// // test manually Update the sample video added earlier
// async function updateSampleVideo() {
//     const videoId = '6676a61dafc32dbcb2a532c4';  // Replace with the actual ID
//     try {
//         const updatedVideo = await updateVideoModel(videoId, { title: 'tryEdit' });
//         console.log('Video updated:', updatedVideo);
//     } catch (error) {
//         console.error('Failed to update video:', error);
//     }
// }
// // test manually Delete the sample video added earlier
// async function deleteSampleVideo() {
//     const videoId = '6676aa67d97f7d21b433c0e3';  // Replace with the actual ID
//     try {
//         const deletedVideo = await deleteVideoModel(videoId);
//         console.log('Video deleted:', deletedVideo);
//     } catch (error) {
//         console.error('Failed to delete video:', error);
//     }
// }



/////////////////////////end of tests///////////////////////

