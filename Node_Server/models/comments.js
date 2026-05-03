import mongoose from 'mongoose';
import { upload } from '../routes/mediaRoutes.js';

const commentSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    videoId: { type: mongoose.Schema.Types.ObjectId, ref: 'Video', required: true },
    text: { type: String, required: true },
    uploadTime : String,
    likes: { type: Number, default: 0 },
    likedByUsers: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }]
});

const Comment = mongoose.model('Comment', commentSchema);
export default Comment;
