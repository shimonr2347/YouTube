import mongoose from "mongoose";    

const Schema = mongoose.Schema 

const newUserSchema = new Schema({
    firstName: String,
    lastName: String,
    date: String,  
    email: String,
    profilePic: String,
    username: {
        type: String,
        required: true,
        unique: true
    },
    password: String,
    timestamp: {
        type: Date,
        default: Date.now
    }
});



export default mongoose.model('User', newUserSchema);
