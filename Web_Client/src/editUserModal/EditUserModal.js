import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function EditUserModal({ user, closeModal, setUser }) {
  const token = sessionStorage.getItem('token');
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    profilePic: user.profilePic,
    username: user.username,
  });

  const [previewProfilePic, setPreviewProfilePic] = useState(user.profilePic);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    const allowedImageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];

    if (file && allowedImageTypes.includes(file.type)) {
      setFormData({ ...formData, profilePic: file });
      setPreviewProfilePic(URL.createObjectURL(file));
      setErrors({ ...errors, profilePic: '' });
    } else {
      setFormData({ ...formData, profilePic: user.profilePic });
      setPreviewProfilePic(user.profilePic);
      setErrors({ ...errors, profilePic: 'Please select a valid image file (JPEG, JPG, PNG, WebP, or GIF).' });
    }
  };

  const handleSubmit = async () => {
    const newErrors = {};

    if (!formData.firstName) {
      newErrors.firstName = 'Please enter your first name.';
    }

    if (!formData.lastName) {
      newErrors.lastName = 'Please enter your last name.';
    }

    if (!formData.email) {
      newErrors.email = 'Please enter your email.';
    }

    if (!formData.profilePic) {
      newErrors.profilePic = 'Please upload a profile picture.';
    }

    if (!formData.username) {
      newErrors.username = 'Please enter a username.';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      const formDataToUpload = new FormData();
      formDataToUpload.append('firstName', formData.firstName);
      formDataToUpload.append('lastName', formData.lastName);
      formDataToUpload.append('email', formData.email);
      formDataToUpload.append('profilePic', formData.profilePic);
      formDataToUpload.append('username', formData.username);

      const response = await fetch(`http://localhost:12345/api/users/${user._id}`, {
        method: 'PUT',
        body: formDataToUpload,
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        const token = data.token;
        const profilePic = data.profilePic;
        const userName = data.username;
        if (userName) {
          sessionStorage.setItem('userName', userName);
        }
        if (profilePic) {
          sessionStorage.setItem('profilePic', profilePic);
        }
        if (token) {
          sessionStorage.setItem('token', token);
        }
        // const profilePic = data.profilePic;
        // const userName = data.username;

        // console.log('Updated user:', updatedUser);
        //save for later
        // setUser({
        //   firstName: userUpdated.firstName,
        //   lastName: userUpdated.lastName,
        //   email: userUpdated.email,
        //   profilePic: userUpdated.profilePic,
        //   username: userUpdated.username,
        // });
        console.log('token is', token);
        // console.log('updated user',formData);
        // i want to update the user in the parent component only in the keys that have been changed
        // setUser(updatedUser);
        // setUser(formData);
        // navigate(`/users/${user._id}`);
        // window.location.reload();
        navigate('/');
        closeModal();
      } else if (response.status === 400) {
        const data = await response.json();
        console.log('Error:', data.message);
        setErrors({ ...errors, username: data.message });
      }
      else {
        console.error('Failed to update user');
      }
    } catch (error) {
      console.error('Failed to update user:', error);
    }
  };

  const handleDelete = async () => {
    if (window.confirm(`Are you sure you want to delete your account, ${user.username}?`)) {
      try {
        const response = await fetch(`http://localhost:12345/api/users/${user._id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (response.ok) {
          console.log('Deleted user');
          sessionStorage.clear();
          navigate('/');
          closeModal();
        } else {
          console.error('Failed to delete user');
        }
      } catch (error) {
        console.error('Failed to delete user:', error);
      }
    }
  };

  return (
    <div className="modal fade show" id="editUserModal" tabIndex="-1" aria-labelledby="editUserModalLabel" aria-hidden="true" style={{ display: 'block' }}>
      <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h1 className="modal-title fs-5" id="editUserModalLabel">Edit User Profile</h1>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close" onClick={closeModal}></button>
          </div>
          <div className="modal-body" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
            <div className="mb-3">
              <img src={previewProfilePic} alt="Current profile picture" className="current-profile-pic rounded-circle  mb-3 ms-5" style={{ maxWidth: '150px', height: '150px' }} />
              <input type="file" className="form-control" name="profilePic" id="profilePic" onChange={handleImageChange} />
              {errors.profilePic && <div className="text-danger">{errors.profilePic}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="firstName" className="col-form-label">First Name:</label>
              <input type="text" className="form-control" id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} />
              {errors.firstName && <div className="text-danger">{errors.firstName}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="lastName" className="col-form-label">Last Name:</label>
              <input type="text" className="form-control" id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} />
              {errors.lastName && <div className="text-danger">{errors.lastName}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="email" className="col-form-label">Email:</label>
              <input type="email" className="form-control" id="email" name="email" value={formData.email} onChange={handleChange} />
              {errors.email && <div className="text-danger">{errors.email}</div>}
            </div>
            <div className="mb-3">
              <label htmlFor="username" className="col-form-label">Username:</label>
              <input type="text" className="form-control" id="username" name="username" value={formData.username} onChange={handleChange} />
              {errors.username && <div className="text-danger">{errors.username}</div>}
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary btn-ops" data-bs-dismiss="modal" onClick={closeModal}>
              <h5 className="ops mb-0">Close</h5>
            </button>
            <button type="button" className="btn btn-primary btn-ops" onClick={handleSubmit}>
              <h5 className="ops mb-0">Save changes</h5>
            </button>
            <button type="button" className="btn btn-danger btn-ops" onClick={handleDelete}>
              <h5 className="ops mb-0">Delete</h5>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EditUserModal;
