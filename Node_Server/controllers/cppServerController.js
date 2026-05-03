import {
    sendWatchNotification, createThreadForUser, closeThreadForUser, getRecommendations,
    getAllVideosWithViewCounts, 
} from '../services/cppServerService.js';
import jwt from 'jsonwebtoken';
import User from '../models/users.js';

const key = "secretkey";


export const getVideoRecommendations = async (req, res) => {
    console.log("Received recommendation request");
    console.log("Request body:", req.body);
    try {
        const { videoId, token } = req.body;
        console.log("Extracted videoId:", videoId);
        console.log("Extracted token:", token);
        let userId = 'guest';
        if (!videoId) {
            console.log("VideoId is missing");
            return res.status(400).json({ message: 'videoId is required' });
        }
        if (token && token !== 'guest') {
            try {
                console.log("Attempting to verify token");
                const decoded = jwt.verify(token, key);
                console.log("Decoded token:", decoded);
                const user = await User.findOne({ username: decoded.username });
                if (user) {
                    userId = user._id.toString();
                    console.log("User found, userId set to:", userId);
                } else {
                    console.log("User not found for decoded username");
                }
            } catch (error) {
                console.error('Error verifying token:', error);
            }
        } else {
            console.log("Using guest userId");
        }

        console.log("Fetching all videos with view counts");
        const allVideos = await getAllVideosWithViewCounts();
        console.log("Fetched", allVideos.length, "videos");

        console.log("Getting recommendations for userId:", userId, "and videoId:", videoId);
        const recommendationsWithDetails = await getRecommendations(userId, videoId, allVideos);
        console.log("Received", recommendationsWithDetails.length, "recommendations");

        res.status(200).json(recommendationsWithDetails);
    } catch (error) {
        console.error('Error getting video recommendations:', error);
        console.error('Error details:', error.message, error.stack);
        res.status(500).json({ message: 'Failed to get video recommendations', error: error.message });
    }
};

export const notifyVideoWatch = async (req, res) => {
    try {
        const userId = req.user.id; 
        const { videoId } = req.body;
       
        if (!videoId) {
            return res.status(400).json({ message: 'videoId is required' });
        }

        const result = await sendWatchNotification(userId, videoId);
        res.status(200).json({ message: 'Watch notification sent successfully', result });
    } catch (error) {
        console.error('Error notifying video watch:', error);
        res.status(500).json({ message: 'Failed to notify video watch' });
    }
};

export const createUserThread = async (req, res) => {
    try {
        const userId = req.user.id.toString(); 
       console.log('in create user thread ---- user id: ',userId)
        const result = await createThreadForUser(userId);
        res.status(200).json({ message: 'Thread created successfully', result });
    } catch (error) {
        console.error('Error creating thread in C++ server:', error);
        res.status(500).json({ message: 'Failed to create thread in C++ server' });
    }
};

export const closeUserThread = async (req, res) => {
    console.log("entered to closeUserThread in server.....")
    try {
        const userId = req.user.id; 
        console.log("userId",userId);
        const result = await closeThreadForUser(userId);
        res.status(200).json({ message: 'Thread closed successfully', result });
    } catch (error) {
        console.error('Error closing thread in C++ server:', error);
        res.status(500).json({ message: 'Failed to close thread in C++ server' });
    }
};

export default notifyVideoWatch;
