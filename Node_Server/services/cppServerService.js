import net from 'net';
import Video from '../models/videoPlay.js';

const CPP_SERVER_HOST = 'localhost';
const CPP_SERVER_PORT = 55551;

function sendRequestToCppServer(action, data) {
    return new Promise((resolve, reject) => {
        const client = new net.Socket();

        client.connect(CPP_SERVER_PORT, CPP_SERVER_HOST, () => {
            console.log('Connected to C++ server');
            const message = JSON.stringify({ action, ...data });
            const request = `POST / HTTP/1.1\r\nContent-Type: application/json\r\nContent-Length: ${message.length}\r\n\r\n${message}`;
            client.write(request);
        });

        client.on('data', (data) => {
            console.log('Received from C++ server:', data.toString());
            const response = data.toString();
            const jsonStartIndex = response.indexOf('{');
            const jsonEndIndex = response.lastIndexOf('}');
            if (jsonStartIndex !== -1 && jsonEndIndex !== -1) {
                const jsonBody = response.slice(jsonStartIndex, jsonEndIndex + 1);
                client.destroy();
                resolve(JSON.parse(jsonBody));
            } else {
                client.destroy();
                resolve(response);
            }
        });

        client.on('close', () => {
            console.log('Connection closed');
        });

        client.on('error', (err) => {
            console.error('Connection error:', err);
            reject(err);
        });
    });
}

export const createThreadForUser = (userId) => {
    return sendRequestToCppServer('create_thread', { userId });
};

export const closeThreadForUser = (userId) => {
    return sendRequestToCppServer('close_thread', { userId });
};

export const sendWatchNotification = (userId, videoId) => {
    return sendRequestToCppServer('notify-watch', { userId, videoId });
};

export const getRecommendations = async (userId, videoId, allVideos) => {
    try {
        const response = await sendRequestToCppServer('get_recommendations', { userId, videoId, allVideos });
        const recommendedVideoIds = response.recommendations;

        // Fetch full details for recommended videos
        const recommendedVideos = await Video.find({ _id: { $in: recommendedVideoIds } })
            .populate('authorId', 'username profilePic')
            .select('thumbnailUrl title views uploadTime category videoUrl');

        // Transform the result to include only the required fields
        const transformedVideos = recommendedVideos.map(video => ({
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
        console.error('Failed to fetch recommended videos:', error);
        throw error;
    }
};

export async function getAllVideosWithViewCounts() {
    try {
        const videos = await Video.find({}, 'id views');
        return videos.map(video => ({ id: video._id.toString(), views: video.views }));
    } catch (error) {
        console.error('Failed to fetch videos with view counts:', error);
        throw error;
    }
}