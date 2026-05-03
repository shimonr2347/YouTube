import React, { useState } from 'react';
// import './AddVideoModal.css';

function AddVideoModal({ closeModal, user }) {
  const token = sessionStorage.getItem('token');
  const [formData, setFormData] = useState({
    thumbnailUrl: null,
    videoUrl: null,
    title: '',
    authorId: user.id,
    authorName: user.username,
    views: 0,
    category: '',
    likes: 0,
  });

  const [previewVideo, setPreviewVideo] = useState(null);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    const allowedImageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];

    if (file && allowedImageTypes.includes(file.type)) {
      setFormData({ ...formData, thumbnailUrl: file });
      setErrors({ ...errors, thumbnailUrl: '' });
    } else {
      setFormData({ ...formData, thumbnailUrl: null });
      setErrors({ ...errors, thumbnailUrl: 'Please select a valid image file (JPEG, JPG, PNG, WebP, or GIF).' });
    }
  };

  const handleVideoChange = (e) => {
    const file = e.target.files[0];
    const allowedVideoTypes = ['video/mp4', 'video/webm', 'video/ogg'];

    if (file && allowedVideoTypes.includes(file.type)) {
      setFormData({ ...formData, videoUrl: file });
      setPreviewVideo(URL.createObjectURL(file));
      setErrors({ ...errors, videoUrl: '' });
    } else {
      setFormData({ ...formData, videoUrl: null });
      setPreviewVideo(null);
      setErrors({ ...errors, videoUrl: 'Please select a valid video file (MP4, WebM, or OGG).' });
    }
  };

  const handleSubmit = async () => {
    const newErrors = {};

    if (!formData.title) {
      newErrors.title = 'Please enter a title.';
    }

    if (!formData.category) {
      newErrors.category = 'Please select a category.';
    }

    if (!formData.thumbnailUrl) {
      newErrors.thumbnailUrl = 'Please upload an image.';
    }

    if (!formData.videoUrl) {
      newErrors.videoUrl = 'Please upload a video.';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      const formDataToUpload = new FormData();
      formDataToUpload.append('video', formData.videoUrl);
      formDataToUpload.append('thumbnail', formData.thumbnailUrl);
      formDataToUpload.append('title', formData.title);
      formDataToUpload.append('authorId', formData.authorId);
      formDataToUpload.append('authorName', formData.authorName);
      formDataToUpload.append('category', formData.category);
      formDataToUpload.append('views', formData.views);
      formDataToUpload.append('likes', formData.likes);

      const response = await fetch('http://localhost:12345/api/users/:id/videos', {
        method: 'POST',
        body: formDataToUpload,
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const newVideo = await response.json();
        console.log('New video:', newVideo);
        // setVideosList([...videosList, newVideo]);
        // setUpdatedList(true);
        closeModal();
      } else {
        console.error('Failed to add video');
      }
    } catch (error) {
      console.error('Failed to add video:', error);
    }
  };

  return (
    <div className="modal fade show" id="addVideoModal" tabIndex="-1" aria-labelledby="addVideoModalLabel" aria-hidden="true" style={{ display: 'block' }}>
      <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h1 className="modal-title fs-5" id="addVideoModalLabel">Add Video</h1>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close" onClick={closeModal}></button>
          </div>
          <div className="modal-body" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
            {formData.thumbnailUrl && (
              <div className="mb-3">
                <label htmlFor="videoThumbnail" className="form-label me-3 video-prev">Image Preview:</label>
                <img src={URL.createObjectURL(formData.thumbnailUrl)} alt="Current video thumbnail" className="current-video-img "  style={{ maxWidth: '100%', height: '200px' }}/>
              </div>
            )}
            {previewVideo && (
              <div className="mb-3">
                <label htmlFor="currentVideo" className="form-label video-prev ">Video Preview:</label>
                <video controls src={previewVideo} className="current-video-clip" style={{ maxWidth: '80%', height: '300px' }}></video>
              </div>
            )}
            <div className="mb-3">
              <label htmlFor="videoTitle" className="col-form-label">Video Title:</label>
              <input type="text" className="form-control" id="videoTitle" name="title" value={formData.title} onChange={handleChange} />
              {errors.title && <div className="text-danger">{errors.title}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="videoCategory" className="col-form-label">Category:</label>
              <select className="form-select" id="videoCategory" name="category" value={formData.category} onChange={handleChange}>
                <option value="">Select a category</option>
                <option value="Sport">Sport</option>
                <option value="News">News</option>
                <option value="Cinema">Cinema</option>
                <option value="Gaming">Gaming</option>
              </select>
              {errors.category && <div className="text-danger">{errors.category}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="videoThumbnail" className="col-form-label">Image:</label>
              <input type="file" className="form-control" name="thumbnailUrl" id="videoThumbnail" onChange={handleImageChange} />
              {errors.thumbnailUrl && <div className="text-danger">{errors.thumbnailUrl}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="videoVideoUrl" className="col-form-label">Video:</label>
              <input type="file" className="form-control" name="videoUrl" id="videoVideoUrl" onChange={handleVideoChange} />
              {errors.videoUrl && <div className="text-danger">{errors.videoUrl}</div>}
            </div>

          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary btn-ops" data-bs-dismiss="modal" onClick={closeModal}>
              <h5 className="ops mb-0">Close</h5>
            </button>
            <button type="button" className="btn btn-primary btn-ops" onClick={handleSubmit}>
              <h5 className="ops mb-0">Save changes</h5>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AddVideoModal;