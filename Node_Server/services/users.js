import User from '../models/users.js';
import Video from '../models/videoPlay.js';
import Comment from '../models/comments.js';



export const createUser = async (newUser) => {
    try {
        const user = new User(newUser);
        await user.save();
        return user; // It's often useful to return the created user object
    } catch (error) {
        throw new Error('Error creating user: ' + error.message);
    }
};



export const findUser = async (username) => {
    try {
        const user = await User.findOne({ username: username });
        return user != null;
    } catch (err) {
        console.error("Error checking username availability:", err);
        throw err;  // Ensure errors are thrown appropriately
    }
};


export const getUserid = async (username) => {
    try {
        const user = await User.findOne({ username: username });
        return user._id;
    }
    catch (error) {
        throw new Error('Error fetching user: ' + error.message);
        throw error;
    }

}


export const getUserSelectedDetails = async (id) => {
    try {
        const user = await getUserbyId(id);
        if (user === null) {
            console.log('User not found');
            throw new Error('User not found');
        } else {
            console.log('User found 1');
            const userObj =
            {
                firstName: user.firstName,
                lastName: user.lastName,
                profilePic: user.profilePic,
                username: user.username,
                email: user.email,
                date: user.date,
                _id: user._id
            }
            // console.log(userObj);
            return userObj;
        }
    } catch (error) {
        throw new Error('Error fetching user: ' + error.message);
    }
}



export const getUserbyId = async (id) => {
    // console.log('hello');
    try {
        const user = await User.findById({ _id: id });
        // console.log(user);
        if (user === null) {
            console.log('User not found');
            throw new Error('User not found');
        } else {
            return user;
        }
    } catch (error) {
        throw new Error('Error fetching user: ' + error.message);
    }
}




//get user by username just like the above function
export const getUserbyUsername = async (username) => {
    try {
        const user = await User.findOne({ username: username });
        // console.log(user);
        if (user === null) {
            console.log('User not found');
            throw new Error('User not found');
        } else {
            //  console.log('User found 1');
            const userObj =
            {
                firstName: user.firstName,
                lastName: user.lastName,
                profilePic: user.profilePic,
                username: user.username,
                password: user.password,
                email: user.email,
                date: user.date,
                id: user._id
            }
            //  console.log(userObj);
            return userObj;
        }
    } catch (error) {
        throw new Error('Error fetching user: ' + error.message);
    }
}

export async function updateUserModel(id, updateData) {
    return await User.findByIdAndUpdate(id, updateData, { new: true });
}


export async function deleteUserModel(id) {
    try {
        const user = await User.findByIdAndDelete(id);
        if (!user) {
            throw new Error('User not found');
        }

        // Fetch and delete all comments made by the user
        const comments = await Comment.find({ userId: id });
        const commentIds = comments.map(comment => comment._id);

        // Remove these comments from the videos
        await Video.updateMany(
            {},
            { $pull: { comments: { $in: commentIds } } }
        );

        try {
            // Delete all comments made by the user
            await Comment.deleteMany({ userId: id });
        } catch (error) {
            throw new Error('Error deleting comments: ' + error.message);
        }
        try {
            // Delete videos authored by the user
            await Video.deleteMany({ authorId: id });
        } catch (error) {
            throw new Error('Error deleting videos: ' + error.message);
        }
        try {
            // Delete user
            await User.findByIdAndDelete(id);
        }
        catch (error) {
            throw new Error('Error deleting user: ' + error.message);
        }
        return true;
    } catch (error) {
        throw new Error('Error deleting user and related data: ' + error.message)
    }
}



// Using default export
export default User;
