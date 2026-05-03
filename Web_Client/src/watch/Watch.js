import React, { useState, useEffect, useCallback } from 'react';
import { useLocation, Link, useNavigate, useParams } from 'react-router-dom';
import './Watch.css';
import axios from 'axios';

function Watch({ darkMode }) {
  const navigate = useNavigate();
  const { id, pid } = useParams();
  const [currentVideo, setCurrentVideo] = useState(null);
  const [recommendedVideos, setRecommendedVideos] = useState([]);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState(''); // To hold new comment text
  const [editingCommentId, setEditingCommentId] = useState(null); // To track the comment being edited
  const [commentEditText, setCommentEditText] = useState(''); // To hold the edit text for comments
  const [isLiked, setIsLiked] = useState(false);
  const [likedBy, setLikedBys] = useState([]);
  const [videoLikes, setVideoLikes] = useState(0);
  const [user, setUser] = useState(null);
  const userName = sessionStorage.getItem('userName');
  const profilePic = sessionStorage.getItem('profilePic');
  const token = sessionStorage.getItem('token');
  const userId = sessionStorage.getItem('userId');



  useEffect(() => {
    if (token && token !== 'null') {
      // console.log('Token found:', token);
      setUser({ username: userName, profilepic: profilePic });
    }
  }, [token, userName, profilePic]);


  useEffect(() => {
    const fetchVideoAndComments = async () => {
      try {
        const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}`);
        if (response.ok) {
          const data = await response.json();
          setCurrentVideo(data);
          //likes handle
          setLikedBys(data.likedBy)
          setVideoLikes(data.likes);
          if (data.likedBy.includes(userId)) {
            setIsLiked(true);
          } else {
            setIsLiked(false);
          }
          //end of likes handle
          // Ensure each comment has an initialized likedByUsers array
          const initializedComments = data.commentsList.map(comment => ({
            ...comment,
            likedByUsers: comment.likedByUsers || [],// Ensure likedByUsers is always an array
            isLiked: token ? (comment.likedByUsers.includes(userId)) : false
          }));
          setComments(initializedComments);
        } else {
          console.error('Failed to fetch video');
          setComments([]); // Reset comments if the video fetch fails
        }
      } catch (error) {
        console.error('Failed to fetch the video , the error is:', error);
        setComments([]); // Reset comments if there's an exception
      }
    };
    fetchVideoAndComments();
  }, [pid, id, token, userId]);  // Depend on token and userId to re-fetch when user logs in/out

  useEffect(() => {
    const fetchRecommendedVideos = async () => {
      try {
        const response = await fetch(`http://localhost:12345/api/cpp/get-recommendations`, {
          method:'POST',
          headers: {
            'Content-Type': 'application/json',  
          },

          body: JSON.stringify({ videoId: pid , token: token}),
      });
        if (response.ok) {
          const videos = await response.json();
          // Filter out the current video from the recommended videos
          const recommendedVideos = videos.filter((video) => video._id !== pid);
          setRecommendedVideos(recommendedVideos);
          // console.log(recommendedVideos)
        } else {
          console.error('Failed to fetch the recommended videos');
        }
      } catch (error) {
        console.error('Failed to fetch the recommended videos:', error);
      }
    };
    const notifyVideoWatch = async () => {
      console.log('token',token);
      if(!token){
        fetchRecommendedVideos();
      }
      if (token && token === 'null'){
        fetchRecommendedVideos();
      }
      try {
        const response = await fetch(`http://localhost:12345/api/cpp/notify-watch`,{
          method:'POST',
          headers: {
              Authorization: `Bearer ${token}`,
              'Content-Type': 'application/json',  
          },
        body: JSON.stringify({ videoId: pid }),
        });
        if (response.ok) {
          fetchRecommendedVideos();
        }else{
          console.error('Failed to notify watch');
        }
      } catch (error) {
        console.error('Failed to notify watch , the error is:', error);
      }
    };
    notifyVideoWatch();
  }, [pid]);


  // useEffect(() => {
  //   const fetchRecommendedVideos = async () => {
  //     try {
  //       const response = await fetch(`http://localhost:12345/api/videos`);
  //       if (response.ok) {
  //         const videos = await response.json();
  //         // Filter out the current video from the recommended videos
  //         const recommendedVideos = videos.filter((video) => video._id !== pid);
  //         setRecommendedVideos(recommendedVideos);
  //         // console.log(recommendedVideos)
  //       } else {
  //         console.error('Failed to fetch the recommended videos');
  //       }
  //     } catch (error) {
  //       console.error('Failed to fetch the recommended videos:', error);
  //     }
  //   };

  //   fetchRecommendedVideos();
  // }, [pid]);

  const handleLikeVideo = async () => {
    //isLiked = currentVideo.likedBy.includes(userId)
    console.log('is user liked:', isLiked);
    console.log('number of likes before: ', videoLikes);
    // const token = localStorage.getItem('token'); // Ensure the token is fetched correctly
    const token = sessionStorage.getItem('token'); // Ensure the token is fetched correctly
    if (!token) {
      console.log('User is not logged in');
      return;
    }
    if (isLiked === false) {
      try {
        console.log('video:', currentVideo._id);
        const response = await axios.put(`http://localhost:12345/api/users/${id}/videos/${pid}/likes`, {}, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.status !== 200) { // Check the response status
          throw new Error('Network response was not ok');
        }
        // console.log('response:', response);
        const updatedVideo = response.data;
        console.log('updatedVideo:', updatedVideo);
        setVideoLikes(updatedVideo.likes);
        setIsLiked(true);
        console.log('Liking video 2:', isLiked);
        console.log('Video liked successfully:', updatedVideo); // Logging the updated video
      } catch (error) {
        console.error('Error liking/unliking video:', error);
      }
    } else {
      console.log('Video already liked');
    }
  }

  const handleUnlikeVideo = async () => {
    const token = sessionStorage.getItem('token'); // Ensure the token is fetched correctly
    if (!token) {
      console.log('User is not logged in');
      return;
    }
    console.log('isLiked at unlike:', isLiked);
    if (isLiked === true) {
      try {
        console.log('Unliking video:', currentVideo._id);
        const response = await axios.put(`http://localhost:12345/api/users/${id}/videos/${pid}/unlikes`, {}, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.status !== 200) {
          throw new Error('Network response was not ok');
        }

        const updatedVideo = response.data;
        console.log('updatedVideo:', updatedVideo);
        setVideoLikes(updatedVideo.likes);
        setIsLiked(false)
        console.log('isLiked at after unlike:', isLiked);
        console.log('Video unliked successfully:', updatedVideo);
      } catch (error) {
        console.error('Error liking/unliking video:', error);
      }
    } else {
      console.log('Video already unliked');
    }
  };







  // const handleAddComment = async () => {
  //   // Ensure there is a new comment to add
  //   console.log(newComment);
  //   if (newComment.trim()) {
  //     const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}/comments`, {
  //       method: 'POST',
  //       headers: {
  //         'Content-Type': 'application/json',
  //         'Authorization': `Bearer ${token}`
  //       },
  //       body: JSON.stringify({ text: newComment })
  //     });
  //     if (response.ok) {
  //       const updatedComments = await response.json();
  //       setComments(updatedComments);
  //       setNewComment(''); // Clear the input after submitting
  //     }
  //   }
  // };
  const handleAddComment = async () => {
    if (newComment.trim()) {
      const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ text: newComment })
      });
      if (response.ok) {
        const newComment = await response.json();  // Assuming server returns the newly added comment
        newComment.userId = { _id: userId, username: userName, profilePic: profilePic};
        setComments(prevComments => [...prevComments, newComment]);  // Append new comment to the existing comments
        setNewComment(''); // Clear the input after submitting
      } else {
        console.error('Failed to add comment');
      }
    }
  };


  const handleDeleteComment = async (commentId) => {
    const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}/comments/${commentId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json', // Not needed for DELETE, but doesn't hurt to have it
        'Authorization': `Bearer ${token}`
      }
    });
    if (response.ok) {
      // Remove the deleted comment from the comments list
      setComments(comments.filter(comment => comment._id !== commentId));
    }
  };
  /*
    const handleEditComment = (commentId) => {
      setEditingCommentId(commentId);
      const comment = comments.find(comment => comment._id === commentId);
      setCommentEditText(comment ? comment.content : '');
    };
  
    const handleSaveEdit = async (commentId) => {
      if (commentEditText.trim()) {
        const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}/comments/${commentId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({ content: commentEditText })
        });
        if (response.ok) {
          const updatedComment = await response.json();
          setComments(comments.map(comment => comment._id === commentId ? { ...comment, content: updatedComment.content } : comment));
          setEditingCommentId(null); // Reset editing state
          setCommentEditText(''); // Clear edit text
        }
      }
    };
    */

  const handleEditComment = (commentId) => {
    const comment = comments.find(comment => comment._id === commentId);
    setCommentEditText(comment ? comment.text : ''); // Make sure to use the correct property for text
    setEditingCommentId(commentId);
  };

  const handleSaveEdit = async (commentId) => {
    if (commentEditText.trim()) {
      const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}/comments/${commentId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ content: commentEditText }) // Ensure the body is structured as expected by the server
      });

      if (response.ok) {
        const updatedComment = await response.json();
        // Update the comments state array
        setComments(comments.map(comment => {
          if (comment._id === commentId) {
            return { ...comment, text: updatedComment.text }; // Make sure the property matches the server response
          }
          return comment;
        }));
        setEditingCommentId(null); // Exit editing mode
        setCommentEditText(''); // Clear the edit text field
      } else {
        console.error('Failed to edit comment');
      }
    }
  };


  // const handleToggleLike = async (commentId, alreadyLiked) => {
  //   const url = `http://localhost:12345/api/users/${id}/videos/${pid}/comments/${commentId}/${alreadyLiked ? 'unLike' : 'like'}`;
  //   try {
  //     const response = await fetch(url, {
  //       method: 'PUT',
  //       headers: {
  //         'Authorization': `Bearer ${token}`
  //       }
  //     });

  //     if (response.ok) {
  //       const updatedComment = await response.json();
  //       // Update comments state to reflect the like status change
  //       setComments(prevComments => prevComments.map(comment => {
  //         if (comment._id === commentId) {
  //           return {
  //             ...comment,
  //             likes: updatedComment.likes,
  //            // likedByUsers: alreadyLiked
  //             likedByUsers: updatedComment.likedByUsers,  // Directly use the server response to set likedByUsers
  //             isLiked: !comment.isLiked  // Toggle the isLiked state
  //               // ? comment.likedByUsers.filter(userId => userId !== userId)
  //               // : [...comment.likedByUsers, userId]
  //           };
  //         }
  //         return comment;
  //       }));
  //     } else {
  //       console.error('Failed to toggle like');
  //     }
  //   } catch (error) {
  //     console.error('Error toggling like:', error);
  //   }
  // };

  const handleToggleLike = async (commentId, alreadyLiked) => {
    const url = `http://localhost:12345/api/users/${id}/videos/${pid}/comments/${commentId}/${alreadyLiked ? 'unlike' : 'like'}`;
    try {
      const response = await fetch(url, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const updatedComment = await response.json();
        // Update the local state to reflect the new like state
        setComments(prevComments => prevComments.map(comment => {
          if (comment._id === commentId) {
            return {
              ...comment,
              likes: updatedComment.likes,
              likedByUsers: updatedComment.likedByUsers,
              isLiked: updatedComment.likedByUsers.includes(userId) // Check if the current user's id is in the updated likedByUsers array
            };
          }
          return comment;
        }));
      } else {
        console.error('Failed to toggle like');
      }
    } catch (error) {
      console.error('Error toggling like:', error);
    }
  };


  return (
    <div className={`video-page container-fluid ${darkMode ? 'dark-mode' : ''}`}>
      <div className='header row'>
        <Link to="/" className="youtube-logo">
            <i className="bi bi-youtube"></i>      
        </Link>
        {/* Existing header buttons */}
      </div>
      <div className='col-1 me-1'/>
      <div className="main-content row align-items-start ms-2">
        <div className="col-7 mt-2">
          <div className="video-section">
            <div className="video-container">
              {currentVideo && (
                <video key={currentVideo._id} controls autoPlay className="w-100">
                  <source src={currentVideo.videoUrl} type="video/mp4" />
                  Your browser does not support the video tag.
                </video>
              )}
            </div>
            <div className="video-info d-flex row">
              <div className="video-details mt-3">
                {currentVideo && (
                  <>
                    <div className="video-details-header">
                      <h5>{currentVideo.title}</h5>
                      <h5>{currentVideo.views} views • {currentVideo.uploadTime} ago</h5>
                    </div>
                    <div className='video-creator d-flex justify-content-between'>
                      <p className='p-0'>
                        <Link to={`/users/${currentVideo.authorId}`}>
                          <img src={currentVideo.authorProfilePic} alt={`${currentVideo.authorName}`} />
                        </Link>{currentVideo.authorName}
                      </p>
                      {/* Add other video details as needed */}
                      {currentVideo && (
                        <>
                          {user ? (
                            <>
                              {isLiked ? (
                                <button
                                  className="btn btn-link-video"
                                  onClick={handleUnlikeVideo}
                                >
                                  <i className="bi bi-hand-thumbs-down"></i>  {videoLikes}
                                </button>
                              ) : (
                                <button
                                  className="btn btn-link-video"
                                  onClick={handleLikeVideo}
                                >
                                  <i className="bi bi-hand-thumbs-up"></i>  {videoLikes}
                                </button>
                              )}
                            </>
                          ) : (
                            <button className="btn btn-link-video">
                              <i className="bi bi-hand-thumbs-up"></i> Like {videoLikes}
                            </button>
                          )}
                        </>
                      )}
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
          <div className="comments-section mt-2">
            <div className="add-comment mt-1">
              {userId ?
                <>
                  <textarea
                    className="form-control mt-1"
                    placeholder="Add a comment..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                  ></textarea>
                  <button onClick={handleAddComment} className="btn btn-primary ms-3 mt-2"><span className='add-comment-btn-text'>Comment</span></button>
                </> :
                <div>
                  <h3 className='mt-5'>Only logged-in users can add comments.</h3>
                </div>
              }
            </div>
            <div className="comments-list mt-4">
              {Array.isArray(comments) && comments.map((comment) => (
                <div key={comment._id} className="comment mt-3">
                  <Link to={`/users/${comment.userId._id}`}>
                  <img src={comment.userId.profilePic} alt={comment.userId.username} className="comment-avatar" />
                  </Link>
                  <div className="comment-body ">
                    <div className="comment-header">
                      <strong>{comment.userId.username}</strong>
                      <span>{comment.uploadTime}</span>
                    </div>
                    {editingCommentId === comment._id ? (
                      <textarea
                        value={commentEditText}
                        onChange={(e) => setCommentEditText(e.target.value)}
                        className="form-control"
                      />
                    ) : (
                      <p>{comment.text}</p>
                    )}
                    <div className="comment-footer">
                      <a onClick={() => handleToggleLike(comment._id, comment.likedByUsers && token && comment.likedByUsers.includes(userId))}
                        className="btn p-0">
                        {token && comment.likedByUsers.includes(userId) ? <i className="bi bi-hand-thumbs-down"></i> : <i className="bi bi-hand-thumbs-up"></i>} {comment.likes}
                      </a>
                      {userId === comment.userId._id && (
                        <div>
                          {editingCommentId === comment._id ? (
                            <a onClick={() => handleSaveEdit(comment._id)} className="btn">Save</a>
                          ) : (
                            <>
                              <a onClick={() => handleEditComment(comment._id)} className="btn me-2">Edit</a>
                              <a onClick={() => handleDeleteComment(comment._id)} className="btn ms-2">Delete</a>
                            </>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div className="col-3 recommendations-section ">
          <div className="recommendations-section-header mt-3 ">
            <h5>Recommended Videos</h5> 
          </div>
          <div className="recommended-videos m-3 ">
            {recommendedVideos.map((recVideo) => (
              <div key={recVideo._id} className="recommended-video">
                <Link to={`/users/${recVideo.authorId}/videos/${recVideo._id}`}>
                  <img src={recVideo.thumbnailUrl} alt={recVideo.title} />
                </Link>
                <div className="recommended-video-info">
                  <h6>{recVideo.title}</h6>
                  <p>
                    {recVideo.author}
                    <br />
                    {recVideo.views} views • {recVideo.uploadTime}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Watch;


//   return (
//     <div className={`video-page container-fluid ${darkMode ? 'dark-mode' : ''}`}>
//       <div className='header row'>
//         {userLoggedIn ?
//           <div className="col d-flex align-items-start">
//             <Link to="/" className="">
//               <button className="utbLogo btn">
//                 <i className="bi bi-youtube"></i>
//               </button>
//             </Link>
//             <div className="nav-item dropdown">
//               <a className="btn btn-action" data-bs-toggle="dropdown" aria-expanded="false">
//                 <img className="btn-user-login btn-user-login-videoPage" src={userLoggedIn.Picture} alt={userLoggedIn.User_name} ></img>
//               </a>
//               <ul className="dropdown-menu">
//                 <li><a type='button' className="dropdown-item" onClick={() => setUserLoggedIn(null)} >Log Out</a></li>
//               </ul>
//             </div>
//           </div>
//           :
//           <div className="col flex align-items-start">
//             <Link to="/" className="">
//               <button className="utbLogo btn">
//                 <i className="bi bi-youtube"></i>
//               </button>
//             </Link>
//             <Link to='/login' className=''>
//               <button className="btn btn-link"><i className="bi bi-person-add"></i></button>
//             </Link>
//           </div>
//         }
//       </div>
//       <div className="main-content row align-items-start">
//         <div className="col-1" />
//         <div className="col-6 mt-4">
//           <div className="video-section">
//             <div className="video-container">
//               <video key={currentVideo.id} controls autoPlay className="w-100">
//                 <source src={currentVideo.video} type="video/mp4" />
//                 Your browser does not support the video tag.
//               </video>
//             </div>
//             <div className="video-info d-flex row">
//               <div className="video-details mt-3">
//                 <div className="video-details-header">
//                   <h5>{currentVideo.title}</h5>
//                 </div>
//                 <div className='video-creator d-flex justify-content-between'>
//                   <p className='p-0'>
//                     <Link to='/user' state={{ userName: currentVideo.creator }}>
//                       <img src={currentVideo.author} alt={currentVideo.creatorImg} />
//                     </Link>{currentVideo.author}</p>
//                   <div className='video-stats '>
//                     <button
//                       className={`btn btn-link-video `}
//                       onClick={() => handleLikeDislikeVideo('like')}
//                     >
//                       <i className="bi bi-hand-thumbs-up "></i> {currentVideo.likes}
//                     </button>
//                     <button className="btn"><i className="bi bi-share"></i></button>
//                   </div>
//                 </div>
//               </div>
//             </div>
//             <div className="comments-section mt-3">
//               <h5>{comments.length} Comments</h5>
//               {userLoggedIn ?
//                 <div className="add-comment">
//                   <textarea
//                     className="form-control mt-1"
//                     placeholder="Add a comment..."
//                     value={newComment}
//                     onChange={(e) => setNewComment(e.target.value)}
//                   ></textarea>
//                   <button onClick={handleAddComment} className="btn btn-primary ms-3 mt-2"><span className='add-comment-btn-text'>Comment</span></button>
//                 </div> :
//                 <div>
//                   <h3 className=' mt-5 msg message-top'> *********************************************************</h3>
//                   <h3 className='msg message-text' > Only users can add comments</h3>
//                   <h3 className='msg mb-5 message-bottom'> *********************************************************</h3>
//                 </div>
//               }
//               <div className="comments-list">
//                 {comments.map(comment => (
//                   <div key={comment.id} className="comment">
//                     <img src={comment.creatorImg} alt={comment.creator} />
//                     <div className="comment-body">
//                       <div className="comment-body-header">
//                         <p><strong>{comment.creator}</strong></p>
//                       </div>
//                       {editingCommentId === comment.id ? (
//                         <textarea
//                           value={commentEditText}
//                           onChange={(e) => setCommentEditText(e.target.value)}
//                           className="editing-comment bg-white text-dark"
//                         />
//                       ) : (
//                         <p>{comment.content}</p>
//                       )}
//                       <div className="comment-actions">
//                         <a onClick={() => handleLikeDislikeComment(comment.id, 'like')} className={` btn btn-link-video ${comment.likes === 1 && userLoggedIn ? 'text-succes' : ''}`}><i className="bi bi-hand-thumbs-up"></i> {comment.likes}</a>
//                         <a onClick={() => handleLikeDislikeComment(comment.id, 'dislike')} className={`btn btn-link-video ${comment.dislikes === 1 && userLoggedIn ? 'text-danger' : ''}`}><i className="bi bi-hand-thumbs-down"></i> {comment.dislikes}</a>
//                         {editingCommentId === comment.id ? (
//                           <button onClick={() => handleSaveEdit(comment.id)} className="btn btn-text">Save</button>
//                         ) : userLoggedIn && (
//                           < div className="comment-actions-opts">
//                             <button onClick={() => handleEditComment(comment.id)} className="btn btn-text">Edit</button>
//                             <button onClick={() => handleDeleteComment(comment.id)} className="btn btn-text">delete</button>
//                           </div>
//                         )}
//                       </div>
//                     </div>
//                   </div>
//                 ))}
//               </div>
//             </div>
//           </div>
//         </div>
//         <div className="col-3 recommendations-section ms-5">
//           <div className="recommendations-section-header mt-2">
//             <h5>Recommended Videos</h5>
//           </div>
//           <div className="recommended-videos mt-3">
//             {recommendedVideos.map((recVideo, index) => (
//               <div key={index} className="recommended-video" onClick={() => handleVideoChange(recVideo)}>
//                 <Link to="/watch" state={{ video: recVideo }}>
//                   <img src={recVideo.img} alt={recVideo.title} />
//                 </Link>
//                 <div className="recommended-video-info">
//                   <h6>{recVideo.title}</h6>
//                   <p>
//                     {recVideo.creator}
//                     <br />
//                     {recVideo.views} views • {recVideo.uploadTime} ago
//                   </p>
//                 </div>
//               </div>
//             ))}
//           </div>
//         </div>
//       </div>
//     </div>
//   );
// }

//export default Watch;




// import React, { useState, useEffect, useCallback } from 'react';
// import { useLocation, Link, useNavigate, useParams } from 'react-router-dom';
// import './Watch.css';
// import axios from 'axios';

// function Watch({ userLoggedIn, setUserLoggedIn, darkMode }) {
//   const navigate = useNavigate();
//   const { id, pid } = useParams();
//   const [isLiked, setIsLiked] = useState(false);
//   const [likedBy, setLikedBys] = useState(null);
//   const [videoLikes, setVideoLikes] = useState(0);
//   const [currentVideo, setCurrentVideo] = useState(null);
//   const [recommendedVideos, setRecommendedVideos] = useState([]);
//   const [comments, setComments] = useState([]);
//   const [user, setUser] = useState(null);

//   const userName = sessionStorage.getItem('userName');
//   const profilePic = sessionStorage.getItem('profilePic');
//   const token = sessionStorage.getItem('token');
//   const userId = sessionStorage.getItem('userId');

//   const [newComment, setNewComment] = useState('');
//   const [commentEditText, setCommentEditText] = useState('');

//   useEffect(() => {
//     if (token !== null) {
//       setUser({ username: userName, profilepic: profilePic });
//     }
    
//   }, [token, userName, profilePic]);

//   const fetchCurrentVideo = useCallback(async () => {
//     try {
//       const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}`);
//       console.log('hello');
//       if (response.ok) {
//         const video = await response.json();
//         console.log(video);
//         setCurrentVideo(video);
//         setComments(video.comments || []);
//       } else {
//         console.error('Failed to fetch the video');
//       }
//     } catch (error) {
//       console.error('Failed to fetch the video:', error);
//     }
//   }, [id, pid, token]);

//   useEffect(() => {
//     fetchCurrentVideo();
//   }, [fetchCurrentVideo]);

//   useEffect(() => {
//     const fetchRecommendedVideos = async () => {
//       try {
//         const response = await fetch(`http://localhost:12345/api/videos`);
//         if (response.ok) {
//           const videos = await response.json();
//           const recommendedVideos = videos.filter((video) => video._id !== pid);
//           setRecommendedVideos(recommendedVideos);
//         } else {
//           console.error('Failed to fetch the recommended videos');
//         }
//       } catch (error) {
//         console.error('Failed to fetch the recommended videos:', error);
//       }
//     };
//     fetchRecommendedVideos();
//   }, [pid, id]);

//   const handleLikeVideo = async () => {
//     // Implementation remains the same
//   };

//   const handleAddComment = async () => {
//     if (newComment.trim() !== '') {
//       if (!token) {
//         console.log('User is not logged in');
//         return;
//       }
//       console.log('Adding comment:', newComment);
//       console.log('user._id:', user._id);
//       console.log('route', `/users/${id}/videos/${pid}/comments`);
//       try {
//         const response = await fetch(`http://localhost:12345/api/users/${id}/videos/${pid}/comments`, {
//           method: 'POST',
//           headers: {
//             'Content-Type': 'application/json',
//             'Authorization': `Bearer ${token}`
//           },
//           body: JSON.stringify({ content: newComment })
//         });
//         if (response.ok) {
//           const newCommentObj = await response.json();
//           const updatedComments = [...comments, newCommentObj];
//           setComments(updatedComments);
//           setNewComment('');
//         } else {
//           console.log('Failed to add comment');
//         }
//       } catch (error) {
//         console.error('Error adding comment:', error);
//       }
//     }
//   };

//   return (
//     <div className={`video-page container-fluid ${darkMode ? 'dark-mode' : ''}`}>
//       <div className='header row'>
//         {/* Header content */}
//       </div>
//       <div className="main-content row align-items-start">
//         <div className="col-1" />
//         <div className="col-8 mt-4">
//           <div className="video-section">
//             <div className="video-container">
//               {currentVideo && (
//                 <video key={currentVideo._id} controls autoPlay className="w-100">
//                   <source src={currentVideo.videoUrl} type="video/mp4" />
//                   Your browser does not support the video tag.
//                 </video>
//               )}
//             </div>
//             <div className="video-info d-flex row">
//               <div className="video-details mt-3">
//                 {currentVideo && (
//                   <>
//                     <div className="video-details-header">
//                       <h5>{currentVideo.title}</h5>
//                       <h5>{currentVideo.views} views • {currentVideo.uploadTime} ago</h5>
//                     </div>
//                     <div className='video-creator d-flex justify-content-between'>
//                       <p className='p-0'>
//                         <Link to={`/users/${currentVideo.authorId}`}>
//                           <img src={currentVideo.authorProfilePic} alt={`${currentVideo.authorName}`} />
//                         </Link>{currentVideo.authorName}
//                       </p>
//                       {user && currentVideo && (
//                         <>
//                           {isLiked ? (
//                             <button className="btn btn-link-video">
//                               <i className="bi bi-hand-thumbs-down"></i> Unlike {videoLikes}
//                             </button>
//                           ) : (
//                             <button className="btn btn-link-video" onClick={handleLikeVideo}>
//                               <i className="bi bi-hand-thumbs-up"></i> Like {videoLikes}
//                             </button>
//                           )}
//                         </>
//                       )}
//                     </div>
//                     <div className="comments-section mt-3">
//                       <h5>{comments.length} Comments</h5>
//                       {user ? (
//                         <div className="add-comment">
//                           <textarea
//                             className="form-control mt-1"
//                             placeholder="Add a comment..."
//                             value={newComment}
//                             onChange={(e) => setNewComment(e.target.value)}
//                           ></textarea>
//                           <button onClick={handleAddComment} className="btn btn-primary ms-3 mt-2">
//                             <span className='add-comment-btn-text'>Comment</span>
//                           </button>
//                         </div>
//                       ) : (
//                         <div>
//                           <h3 className='mt-5 msg message-top'>*********************************************************</h3>
//                           <h3 className='msg message-text'>Only users can add comments</h3>
//                           <h3 className='msg mb-5 message-bottom'>*********************************************************</h3>
//                         </div>
//                       )}
//                     </div>
//                   </>
//                 )}
//               </div>
//             </div>
//           </div>
//         </div>
//         <div className="col-3 recommendations-section ms-5">
//           <div className="recommendations-section-header mt-2">
//             <h5>Recommended Videos</h5>
//           </div>
//           <div className="recommended-videos mt-3">
//             {recommendedVideos.map((recVideo) => (
//               <div key={recVideo._id} className="recommended-video">
//                 <Link to={`/users/${recVideo.authorId}/videos/${recVideo._id}`}>
//                   <img src={recVideo.thumbnailUrl} alt={recVideo.title} />
//                 </Link>
//                 <div className="recommended-video-info">
//                   <h6>{recVideo.title}</h6>
//                   <p>
//                     {recVideo.author}
//                     <br />
//                     {recVideo.views} views • {recVideo.uploadTime}
//                   </p>
//                 </div>
//               </div>
//             ))}
//           </div>
//         </div>
//       </div>
//     </div>
//   );
// }

// export default Watch;