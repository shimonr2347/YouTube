import mongoose from "mongoose";

const Schema = mongoose.Schema


const videoPlaySchema = new Schema({
    thumbnailUrl: String,
    title: String,
    authorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },  
    authorName : String, 
    views: Number,
    uploadTime: String,
    videoUrl: String,
    category: String,
    likes: Number,
    likedBy: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],  
    comments: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Comment' }]  
});



export default mongoose.model('videoPlay', videoPlaySchema);
