import React, { useState, useEffect } from 'react';
import './User.css';
import { useParams, Link } from 'react-router-dom';
import EditUserModal from '../editUserModal/EditUserModal';

function User({ videosList, darkMode, isUserLoggedIn }) {
    const { id } = useParams();
    const [user, setUser] = useState(null);
    const [videos, setVideos] = useState([]);
    const [showEditModal, setShowEditModal] = useState(false);
    const userId = sessionStorage.getItem('userId');

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await fetch(`http://localhost:12345/api/users/${id}/page`);
                if (response.ok) {
                    const user = await response.json();
                    setUser(user);
                } else {
                    console.error('Failed to connect to the server');
                }
            } catch (error) {
                console.error('Failed to fetch user:', error);
            }
        };

        fetchUser();
    }, [id]);

    useEffect(() => {
        const fetchVideos = async () => {
            try {
                const response = await fetch(`http://localhost:12345/api/users/${id}/videos`);
                if (response.ok) {
                    const videos = await response.json();
                    setVideos(videos);
                } else {
                    console.error('Failed to fetch the videos');
                }
            } catch (error) {
                console.error('Failed to fetch videos:', error);
            }
        };

        fetchVideos();
    }, [id]);

    const handleEditClick = () => {
        setShowEditModal(true);
    };

    const closeEditModal = () => {
        setShowEditModal(false);
    };

    return (
        <div className={`user-page  ${darkMode ? 'dark-mode' : ''}`}>
            {user && (
                <>
                    <div className="channel-banner">
                        <h1>{user.username}</h1>
                    </div>
                    <div className="user-info">
                        <img src={user.profilePic} className="profile-pic" alt="Profile" />
                        { userId===user._id && 
                            <button className="btn btn-edit-user" onClick={handleEditClick}>
                                <i className="bi bi-pencil"></i>
                            </button>
                        }
                        <div className="user-details">
                            <h1>{user.username}</h1>
                            <p>225k subscribers</p>
                            <p>Welcome to {user.username}'s page</p>
                        </div>
                       
                    </div>
                </>
            )}
            <nav className="channel-nav">
                <a href="#">  Home</a>
                <a href="#">Videos</a>
                <a href="#">Playlists</a>
                <a href="#">Community</a>
                <a href="#">Channels</a>
                <a href="#">About</a>
                <Link to="/" className="youtube-logo btn">
                    <i className="bi bi-youtube "></i>
                </Link>
            </nav>
            <div className="video-grid ms-5 me-5">
                {videos.map((video) => (
                    <div className="video-card" key={video._id}>
                        <Link to={`/users/${video.authorId}/videos/${video._id}`}>
                            <img src={video.thumbnailUrl} alt="Video thumbnail" />
                        </Link>
                        <div className="video-info">
                            <h3>{video.title}</h3>
                            <span>{video.views} views • {video.uploadTime}</span>
                        </div>
                    </div>
                ))}
            </div>

            {showEditModal && <EditUserModal user={user} closeModal={closeEditModal} setUser={setUser} />}
        </div>
    );
}

export default User;