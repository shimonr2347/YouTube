import {
    createCommentModel, editCommentModel, deleteCommentModel, LikeComment, UnlikeComment,
    isUserLikedComment, fetchUserLikes, resetLikesForGuest
} from '../services/comments.js';

export const addComment = async (req, res) => {
    try {
        console.log('req.body:', req.body);

        const commentData = {
            ...req.body,
            Video: req.params.pid,
            text: req.body.text,
            User: req.user.id,
            videoId: req.params.pid,
            userId: req.user.id
        }
        console.log('commentData:', commentData);
        const updatedVideo = await createCommentModel(commentData);
        res.status(200).json(updatedVideo);
    } catch (error) {
        res.status(500).send(error.message);
    }
}

export const deleteComment = async (req, res) => {
    try {
        //const pid = req.params.pid;
        const commentId = req.params.cid;
        const updatedVideo = await deleteCommentModel(commentId);
        res.status(200).json(updatedVideo);
    } catch (error) {
        res.status(500).send(error.message);
    }
}

export const updateComment = async (req, res) => {
    const { cid } = req.params;
    const { content, text } = req.body;
    const commentText = content || text; // Use content if available, otherwise use text

    console.log('commentText:', commentText);

    if (!commentText) {
        return res.status(400).json({ message: "Comment text is required" });
    }

    try {
        const comment = await editCommentModel(cid, commentText);
        res.status(200).json(comment);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
};

//deal with get isUserLikedcomment in services/comments.js
export const getUserLikedComment = async (req, res) => {
    const { pid, cid } = req.params;
    const userId = req.user.userId;
    //if it is a guest, then isLiked will be false
    if (!userId) {
        return res.json({ isLiked: false });
    }

    try {
        const comment = await isUserLikedComment(pid, cid, userId);
        res.status(200).json(comment);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
};


export const likeComment = async (req, res) => {
    const { pid, cid } = req.params;
    const userId = req.user.id;

    try {
        const comment = await LikeComment(cid, userId);
        res.status(200).json(comment);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
};

export const unlikeComment = async (req, res) => {
    const { pid, cid } = req.params;
    const userId = req.user.id;

    try {
        const comment = await UnlikeComment(cid, userId);
        res.status(200).json(comment);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
};

export const getUserLikes = async (req, res) => {
    try {
        const userId = req.params.userId;
        const likedComments = await commentService.fetchUserLikes(userId);
        res.json({ likedComments });
    } catch (error) {
        res.status(500).json({ message: 'Failed to fetch like states', error: error.message });
    }
};

export const resetGuestLikes = async (req, res) => {
    try {
        const defaultLikes = await commentService.resetLikesForGuest();
        res.json(defaultLikes);
    } catch (error) {
        res.status(500).json({ message: 'Failed to reset likes for guest', error: error.message });
    }
};

