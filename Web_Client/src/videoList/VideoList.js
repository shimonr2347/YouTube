  import React from 'react';
  import VideoItem from '../videoItem/VideoItem';

  function VideoList({ user , setEdit,updatedList}) {

    const videos = updatedList.map((video) => (
      // console.log(video),
      <VideoItem
        key={video._id}
        video={video}
        setEdit={setEdit}
        user = {user}
      />
    ));

    return (
      <div className="row gx-3">
        {videos} 
      </div>
    );
  }

  export default VideoList;
