import { Link } from 'react-router-dom';
import './VideoItem.css';

function VideoItem({  video ,user, setEdit }) {
  const token = sessionStorage.getItem('token');
  const handleEditClick = () => {
    setEdit(video);
  };

  const handelDeleteVideo = () => {
    return async () => {
      try {
        const response = await fetch(`http://localhost:12345/api/users/${video.authorId}/videos/${video._id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        });
        if (response.ok) {
          console.log('Video deleted successfully');
          window.location.reload('/');
        } else {
          console.error('Failed to delete the video');
        }
      } catch (error) {
        console.error('Failed to delete the video:', error);
      }
    };
  }
  // console.log(video._id)
  return (
    <div className="col-lg-3 col-md-4 col-sm-6 mb-4 ">
      <div className="card h-100 video-img">
        <Link to={`/users/${video.authorId}/videos/${video._id}`} >
          <img src={video.thumbnailUrl} className="card-img-top" />
        </Link>
        <div className="card-body video-details">
          <div className="d-flex justify-content-between ">
            <div className="d-flex ">
              <Link to={`users/${video.authorId}`}>
                <img src={video.authorProfilePic} alt={`Profile picture of ${video.author}`} className="creator-img" />
              </Link>
              <div className="ml-2">
                <p className="card-title">{video.title}</p>
                <p className="card-creator-name">{video.author}<br />{video.views} views • {video.uploadTime} </p>
              </div>
            </div>
            { user && user.username===video.author && (
              <div className="nav-item dropdown ">
                <button className="nav-link dropdown p-0  b-0" data-bs-toggle="dropdown" href="#" aria-expanded="false">
                  <i className="bi bi-three-dots-vertical"></i>
                </button>
                <ul className="dropdown-menu">
                  <li>
                    <button type='button' className="dropdown-item" onClick={handleEditClick}>Edit video</button>
                  </li>
                  <li>
                    <button type='button' onClick={handelDeleteVideo()} className="dropdown-item">Delete video</button>
                  </li>
                </ul>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default VideoItem;