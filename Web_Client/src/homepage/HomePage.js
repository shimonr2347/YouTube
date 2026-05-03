// /*
// import React, { useState, useEffect, useContext } from 'react';
// import Search from '../search/Search';
// import VideoList from '../videoList/VideoList';
// import EditVideoModal from '../editVideoModal/EditVideoModal';
// import AddVideoModal from '../addVideoModal/AddVideoModal';
// import { Link } from 'react-router-dom';
// import './HomePage.css';
// import Menu from '../menu/Menu';
// // import { UserContext } from '../UserContext';
// // import { Context } from '../Context';

// function HomePage({ darkMode, setDarkMode }) {
//   // const [user, setUser] = useContext(UserContext);
//   const [user, setUser] = useState(null);



//   // console.log(user);
//   const [base64Image, setBase64Image] = useState('');
//   const [videosList, setVideosList] = useState([]);
//   const [updatedList, setUpdatedList] = useState([]);
//   const [selectedVideo, setSelectedVideo] = useState(null);
//   const [query, setQuery] = useState('');


//   const [selectedCategory, setSelectedCategory] = useState('');


//   const [isAddVideoModalOpen, setIsAddVideoModalOpen] = useState(false);
//   const [isOffcanvasOpen, setIsOffcanvasOpen] = useState(false);

//   const userName = sessionStorage.getItem('userName');
//   const profilePic = sessionStorage.getItem('profilePic');
//   const token = sessionStorage.getItem('token');


//   useEffect(() => {
//     if (token !== null) {
//       // console.log('Token found:', token);
//       setUser({ username: userName, profilepic: profilePic });
//     }
//   }, []);



//   const handleLogOut = () => {
//     sessionStorage.removeItem('token');
//     sessionStorage.removeItem('userName');
//     sessionStorage.removeItem('profilePic');
//     setUser(null);

//   }

//   const handleImageUpload = (event) => {
//     const file = event.target.files[0];
//     const reader = new FileReader();
//     reader.onload = () => {
//       setBase64Image(reader.result);
//     };
//     reader.readAsDataURL(file);
//   };

//   useEffect(() => {
//     const fetchVideos = async () => {
//       try {
//         const response = await fetch('http://localhost:12345/api/videos');
//         if (response.ok) {
//           const videos = await response.json();
//           setVideosList(videos);
//           setUpdatedList(videos);
//         } else {
//           console.error('Failed to fetch the videos');
//         }
//       } catch (error) {
//         console.error('Failed to fetch videos:', error);
//       }
//     };

//     fetchVideos();
//   }, []);
//   // useEffect( async () => {
//   //   try {
//   //     const response = await fetch(`http://localhost:12345/api/videos`,        
//   //       {
//   //         method: 'GET',
//   //         headers: {
//   //           'Content-Type': 'application/json',
//   //         },
//   //       }
//   //     );
//   //     if (response.ok) {
//   //       const videos = await response.json();
//   //       // console.log(data);
//   //       setVideosList(videos);
//   //       setUpdatedList(videos);
//   //     } else {
//   //       console.error('Failed to fetch the videos');
//   //     }
//   //   } catch (error) {
//   //     console.error('Failed to fetch videos:', error);
//   //   }
//   //   }, []);



//   const handleCategoryClick = async (category) => {
//     try {
//       const response = await fetch(`http://localhost:12345/api/videos/category/${category}`);
//       if (!response.ok) {
//         throw new Error('Failed to fetch videos by category');
//       }
//       const videos = await response.json();
//       console.log(videos);
//       setUpdatedList(videos);
//       setSelectedCategory(category); // Optionally, store the selected category for UI purposes
//     } catch (error) {
//       console.error('Error fetching videos by category:', error);
//     }
//   };
//   const doSearch = (q) => {
//     setQuery(q);
//     const updatedList = videosList
//     if (q) {
//       setUpdatedList(videosList.filter((video) => video.title.toLowerCase().startsWith(q.toLowerCase())));
//     } else {
//       setUpdatedList(updatedList);
//     }
//   };


//   const toggleDarkMode = () => {
//     setDarkMode(!darkMode);
//   };




//   // const doSearch = async (q) => {
//   //   try {
//   //     const response = await fetch(`http://localhost:12345/api/videos/search?q=${encodeURIComponent(q)}`);
//   //     if (!response.ok) {
//   //       throw new Error('Failed to fetch videos by search query');
//   //     }
//   //     const videos = await response.json();
//   //     console.log(videos);
//   //     setVideosList(videos); // Update the state with fetched videos
//   //   } catch (error) {
//   //     console.error('Error fetching videos by search query:', error);
//   //   }
//   // };


//   // const doSearch = (q) => {
//   //   if (q) {
//   //     setVideosList(updatedList.filter((video) => video.title.toLowerCase().includes(q.toLowerCase())));
//   //   } else {
//   //     setVideosList(updatedList);
//   //   }
//   // };

//   const deleteVideo = (id) => {
//     const updatedVideos = videosList.filter((video) => video.id !== id);
//     setVideosList(updatedVideos);
//     setUpdatedList(updatedVideos);
//   };


//   const resetSearch = () => {
//     setVideosList(updatedList);
//   };

//   const toggleOffcanvas = () => {
//     setIsOffcanvasOpen(!isOffcanvasOpen);
//   };



//   return (
//     <div className={` container-fluid me-0 ${darkMode ? 'dark-mode' : ''}`}>
//       <div className="row align-items-center fixed-top bg-light ">
//         <div className="col d-flex justify-content-start">
//           <button className="btn menu-expand-btn" onClick={toggleOffcanvas}>
//             <i className="bi bi-list"></i>
//           </button>
//           <button className="utbLogo btn btn-action" >
//             <i className="bi bi-youtube"></i>
//           </button>
//         </div>
//         <div className="col text-center">
//           <Search doSearch={doSearch} />
//         </div>
//         {user ?
//           <div className="col d-flex align-items-end justify-content-end">
//             <button className="btn btn-action" onClick={toggleDarkMode}><i className={` ${darkMode ? 'bi bi-brightness-high-fill' : 'bi bi-moon-stars-fill'}`}  ></i></button>
//             <div className="nav-item dropdown">
//               <a className="btn btn-action " data-bs-toggle="dropdown" aria-expanded="false">
//                 {/* <input type="file" onChange={handleImageUpload} /> */}
//                 {base64Image && (
//                   <img
//                     src={base64Image}
//                     alt="Uploaded"
//                     style={{ maxWidth: '100%', height: 'auto' }}
//                   />
//                 )}
//                 <img className="root btn-user-login " src={user.profilepic} alt={user.profilepic} ></img>
//               </a>
//               <ul className="dropdown-menu">
//                 <li><a type='button' className="dropdown-item" onClick={handleLogOut} >Log Out</a></li>
//               </ul>
//             </div>
//             <button className="btn btn-action upload-video" onClick={() => setIsAddVideoModalOpen(true)}>
//               <i className="bi bi-plus-circle-fill"></i>
//             </button>
//           </div>
//           :
//           <div className="col d-flex align-items-end justify-content-end">
//             <button className="btn btn-action" onClick={toggleDarkMode}><i className={` ${darkMode ? 'bi bi-brightness-high-fill' : 'bi bi-moon-stars-fill'}`}  ></i></button>
//             <Link to='/login'>
//               <a className="btn btn-action btn-login btn-link-primary"><i className="bi bi-person-add"></i></a>
//             </Link>
//           </div>

//         }
//       </div>
//       <div className="tags-list flex d-col text-center mt-2">
//         <button className='teg-item ms-5' onClick={() => handleCategoryClick('Sport')}> Sport</button>
//         <button className='teg-item ms-5' onClick={() => handleCategoryClick('Cinema')}> Cinema</button>
//         <button className='teg-item ms-5' onClick={() => handleCategoryClick('News')}> News</button>
//         <button className='teg-item ms-5' onClick={() => handleCategoryClick('Gaming')}> Gaming</button>
//       </div>

//       <div className="row">
//         <Menu isOffcanvasOpen={isOffcanvasOpen} resetSearch={resetSearch} toggleOffcanvas={toggleOffcanvas} />
//         <div className="col main content mt-3 ms-5">
//           <VideoList setEdit={setSelectedVideo}
//             onDelete={deleteVideo}
//             user={user}
//             videosList={videosList}
//             // setVideosList={setVideosList}
//             updatedList={updatedList}
//             setUpdatedList={setUpdatedList}
//           />
//         </div>
//       </div>
//       {user && selectedVideo && (
//         <EditVideoModal className="d-mode"
//           video={selectedVideo}
//           closeModal={() => setSelectedVideo(null)}
//           videosList={videosList}
//           setVideosList={setVideosList}
//           updatedList={updatedList}
//           setUpdatedList={setUpdatedList}
//         />
//       )}
//       {user && isAddVideoModalOpen && (
//         <AddVideoModal
//           closeModal={() => setIsAddVideoModalOpen(false)}
//           user={user}
//           videosList={videosList}
//           setVideosList={setVideosList}
//           setUpdatedList={setUpdatedList}
//         />
//       )}
//     </div>
//   );
// }

// export default HomePage;


// */




import React, { useState, useEffect, useContext } from 'react';
import Search from '../search/Search';
import VideoList from '../videoList/VideoList';
import EditVideoModal from '../editVideoModal/EditVideoModal';
import AddVideoModal from '../addVideoModal/AddVideoModal';
import { Link } from 'react-router-dom';
import './HomePage.css';
import Menu from '../menu/Menu';
// import { UserContext } from '../UserContext';
// import { Context } from '../Context';

function HomePage({ darkMode, setDarkMode }) {

  const [user, setUser] = useState(null);
  const [base64Image, setBase64Image] = useState('');
  const [videosList, setVideosList] = useState([]);
  const [updatedList, setUpdatedList] = useState([]);
  const [selectedVideo, setSelectedVideo] = useState(null);
  const [query, setQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  const [isAddVideoModalOpen, setIsAddVideoModalOpen] = useState(false);
  const [isOffcanvasOpen, setIsOffcanvasOpen] = useState(false);

  const userName = sessionStorage.getItem('userName');
  const profilePic = sessionStorage.getItem('profilePic');
  const token = sessionStorage.getItem('token');
  const userId = sessionStorage.getItem('userId');


  useEffect(() => {
    if (token && token !== 'null') {
      // console.log('Token found:', token);
      setUser({ username: userName, profilepic: profilePic, id: userId });
    }
  }, []);



  const handleLogOut = async () => {
    try {
      const response = await fetch(`http://localhost:12345/api/cpp/close-thread`,{
        method:'POST',
        headers: {
          Authorization: `Bearer ${token}`,
      },
      });
      if (response.ok) {
        sessionStorage.setItem('userId', null);
        sessionStorage.setItem('token', null);
        sessionStorage.setItem('userName', null);
        sessionStorage.setItem('profilePic', null);
        setUser(null);
      } else{
        throw new Error('Failed to logging out');
      }
    } catch (error) {
      console.error('Error connecting to the cpp server:', error);
    }
  };
  
    
  const handleImageUpload = (event) => {
    const file = event.target.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      setBase64Image(reader.result);
    };
    reader.readAsDataURL(file);
  };

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        const response = await fetch('http://localhost:12345/api/videos');
        if (response.ok) {
          const videos = await response.json();
          setVideosList(videos);
          setUpdatedList(videos);
        } else {
          console.error('Failed to fetch the videos');
        }
      } catch (error) {
        console.error('Failed to fetch videos:', error);
      }
    };

    fetchVideos();
  }, []);


  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };


 
  const handleCategoryClick = async (category) => {
    try {
      const response = await fetch(`http://localhost:12345/api/videos/category/${category}`);
      if (!response.ok) {
        throw new Error('Failed to fetch videos by category');
      }
      const videos = await response.json();
      console.log(videos);
      setUpdatedList(videos);
      setSelectedCategory(category); // Optionally, store the selected category for UI purposes
    } catch (error) {
      console.error('Error fetching videos by category:', error);
    }
  };
  
  const doSearch = (q) => {
    setQuery(q);
    const updatedList = videosList
    if (q) {
      setUpdatedList(videosList.filter((video) => video.title.toLowerCase().startsWith(q.toLowerCase())));
    } else {
      setUpdatedList(updatedList);
    }
  };


  const resetSearch = () => {
    setVideosList(updatedList);
  };

  const toggleOffcanvas = () => {
    setIsOffcanvasOpen(!isOffcanvasOpen);
  };



  return (
    <div className={` container-fluid me-0 ${darkMode ? 'dark-mode' : ''}`}>
      <div className="row align-items-center fixed-top bg-light ">
        <div className="col d-flex justify-content-start">
          <button className="btn menu-expand-btn" onClick={toggleOffcanvas}>
            <i className="bi bi-list"></i>
          </button>
          <button className="utbLogo btn btn-action" >
            <i className="bi bi-youtube"></i>
          </button>
        </div>
        <div className="col text-center">
          <Search doSearch={doSearch} />
        </div>
        {user ?
          <div className="col d-flex align-items-end justify-content-end">
            <button className="btn btn-action" onClick={toggleDarkMode}><i className={` ${darkMode ? 'bi bi-brightness-high-fill' : 'bi bi-moon-stars-fill'}`}  ></i></button>
            <div className="nav-item dropdown">
              <a className="btn btn-action " data-bs-toggle="dropdown" aria-expanded="false">
                {/* <input type="file" onChange={handleImageUpload} /> */}
                {base64Image && (
                  <img
                    src={base64Image}
                    alt="Uploaded"
                    style={{ maxWidth: '100%', height: 'auto' }}
                  />
                )}
                <img className="root btn-user-login " src={user.profilepic} alt={user.profilepic} ></img>
              </a>
              <ul className="dropdown-menu">
                <li><a type='button' className="dropdown-item" onClick={handleLogOut} >Log Out</a></li>
              </ul>
            </div>
            <button className="btn btn-action upload-video" onClick={() => setIsAddVideoModalOpen(true)}>
              <i className="bi bi-plus-circle-fill"></i>
            </button>
          </div>
          :
          <div className="col d-flex align-items-end justify-content-end">
            <button className="btn btn-action" onClick={toggleDarkMode}><i className={` ${darkMode ? 'bi bi-brightness-high-fill' : 'bi bi-moon-stars-fill'}`}  ></i></button>
            <Link to='/login'>
              <a className="btn btn-action btn-login btn-link-primary"><i className="bi bi-person-add"></i></a>
            </Link>
          </div>

        }
      </div>
      <div className="tags-list flex d-col text-center mt-2">
        <button className='teg-item ms-5' onClick={() =>handleCategoryClick('Sport')}> Sport</button>
        <button className='teg-item ms-5' onClick={() =>handleCategoryClick('Cinema')}> Cinema</button>
        <button className='teg-item ms-5' onClick={() =>handleCategoryClick('News')}> News</button>
        <button className='teg-item ms-5' onClick={() =>handleCategoryClick('Gaming')}> Gaming</button>
      </div>
      <div className="row">
        <Menu isOffcanvasOpen={isOffcanvasOpen} resetSearch={resetSearch} toggleOffcanvas={toggleOffcanvas} />
        <div className="col main content mt-3 ms-5">
          <VideoList setEdit={setSelectedVideo}
            user={user}
            updatedList={updatedList}
          />
        </div>
      </div>
      {user && selectedVideo && (
        <EditVideoModal className="d-mode"
          video={selectedVideo}
          closeModal={() => setSelectedVideo(null)}
        />
      )}
      {user && isAddVideoModalOpen && (
        <AddVideoModal
          closeModal={() => setIsAddVideoModalOpen(false)}
          user={user}
        />
      )}
    </div>
  );
}

export default HomePage;