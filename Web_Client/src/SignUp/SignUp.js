import React, { useContext, useState } from 'react';
import './SignUp.css';
import { UserContext } from '../UserContext';
import { useNavigate } from 'react-router-dom';


function SignUp({ darkMode }) {
    // const { userList, addUser } = useContext(UserContext);
    const [newUser, setNewUser] = useState({
        firstName: '',
        lastName: '',
        email: '',
        date: '',
        username: '',
        profilePic: '',
        password: '',
        Password_Confirm: ''
    });
    const [imageFile, setImageFile] = useState(null);
    const navigate = useNavigate();
    const [error, setError] = useState('');
    const [imagePreview, setImagePreview] = useState(null);


    const handleChange = (e) => {
        const { name, files } = e.target;
        if (name === "profilePic" && files.length > 0) {
            const file = files[0];
            console.log(file);
            const validImageTypes = ['image/webp','image/jpg','image/jpeg', 'image/png', 'image/gif']; // Add or remove types as needed

            if (validImageTypes.includes(file.type)) {
                setImageFile(file);
                console.log(file);
                console.log(file);
                setImagePreview(URL.createObjectURL(file))
                setNewUser({ ...newUser, [name]: file }); // Store the file object directly
                setError(''); // Clear any existing error
            } else {
                setError('Please upload a valid image file (JPEG, PNG, or GIF).');
                setImageFile(null);
                e.target.value = ''; // Reset the file input
            }
        } else {
            const { name, value } = e.target;
            setNewUser({ ...newUser, [name]: value });
            console.log(newUser);
        }
    };


    // const handleChange = (e) => {
    //     const { name, files } = e.target;
    //     if (name === "profilePic" && files.length > 0) {
    //         const file = files[0];
    //         const validImageTypes = ['image/jpeg', 'image/png', 'image/gif']; // Add or remove types as needed

    //         if (validImageTypes.includes(file.type)) {
    //             setImageFile(file);
    //             setImagePreview(URL.createObjectURL(file)); // Display image preview if needed
    //             setNewUser({ ...newUser, [name]: file }); // Store the file object directly
    //             setError(''); // Clear any existing error
    //         } else {
    //             setError('Please upload a valid image file (JPEG, PNG, or GIF).');
    //             setImageFile(null);
    //             e.target.value = ''; // Reset the file input
    //         }
    //     } else {
    //         const { name, value } = e.target;
    //         setNewUser({ ...newUser, [name]: value });
    //     }
    // };



    // const handleChange = (e) => {
    //     const { name, value, files } = e.target;
    //     if (name === "profilePic" && files.length > 0) {
    //         const file = files[0];
    //         const validImageTypes = ['image/jpeg', 'image/png', 'image/gif']; // Add or remove types as needed

    //         if (validImageTypes.includes(file.type)) {
    //             setImageFile(file);
    //             const reader = new FileReader();
    //             reader.onloadend = () => {
    //                 setImagePreview(reader.result);
    //                 setNewUser({ ...newUser, [name]: reader.result }); // Store base64 string
    //             };
    //             reader.readAsDataURL(file);
    //             setError(''); // Clear any existing error
    //         } else {
    //             setError('Please upload a valid image file (JPEG, PNG, or GIF).');
    //             setImageFile(null);
    //             setImagePreview(null);
    //             e.target.value = ''; // Reset the file input
    //         }
    //     } else {
    //         setNewUser({ ...newUser, [name]: value });
    //     }
    // };
    const handleSubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData();
        formData.append('firstName', newUser.firstName);
        formData.append('lastName', newUser.lastName);
        formData.append('date', newUser.date);
        formData.append('email', newUser.email);
        formData.append('username', newUser.username);
        formData.append('password', newUser.password);
        formData.append('Password_Confirm', newUser.Password_Confirm);
        formData.append('profilePic', imageFile); // Append the File object
    
        try {
            const response = await fetch('http://localhost:12345/api/users', {
                method: 'POST',
                body: formData,
            });
    
            if (response.status === 201) {
                const data = await response.json();
                console.log(data);
                console.log('Signup successful');
                navigate('/login'); // Navigate to the login page
            } else {
                const errorData = await response.json();
                console.log('Signup failed:', errorData.message);
                setError(errorData.message || 'SignUp failed. Please try again.');
            }
        } catch (error) {
            console.error('Error during Signup:', error);
            setError('An error occurred. Please try again.');
        }
    };


    // const handleSubmit = async (e) => {
    //     e.preventDefault();
    //     const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

    //     if (!passwordRegex.test(newUser.password)) {
    //         setError('Password must contain at least one uppercase letter, one lowercase letter, one number and one special character.');
    //         return;
    //     }

    //     if (newUser.password !== newUser.Password_Confirm) {
    //         setError('Passwords do not match.');
    //         return;
    //     }
    //     try {
    //         const response = await fetch('http://localhost:12345/api/users', {
    //             method: 'POST',
    //             headers: {
    //                 'Content-Type': 'application/json',
    //             },
    //             body: JSON.stringify({ newUser }),
    //         });
    //         if (response.status === 201) {
    //             const data = await response.json();
    //             console.log(data);
    //             console.log('Signup successful');
    //             navigate('/login'); // Navigate to the login page
    //         } else {
    //             const errorData = await response.json();
    //             console.log('Signup failed:', errorData.message);
    //             setError(errorData.message || 'SignUp failed. Please try again.');
    //         }
    //     } catch (error) {
    //         console.error('Error during Sigup:', error);
    //         setError('An error occurred. Please try again.');
    //     }
    // }

    // const handleSubmit = (e) => {
    //     e.preventDefault();
    //     const user = userList.find(
    //         (user) => user.username === newUser.username);

    //     if (user) {
    //         setError('Choose a differenet user name, this user name is allready taken.');
    //         return;
    //     }

    //     const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

    //     if (!passwordRegex.test(newUser.Password)) {
    //         setError('Password must contain at least one uppercase letter, one lowercase letter, one number and one special character.');
    //         return;
    //     }

    //     if (newUser.Password !== newUser.Password_Confirm) {
    //         setError('Passwords do not match.');
    //         return;
    //     }
    //     if (imageFile) {
    //         const reader = new FileReader();
    //         reader.onloadend = () => {
    //             addUser({ ...newUser, Picture: reader.result });
    //             resetForm();
    //         };
    //         reader.readAsDataURL(imageFile);
    //     } else {
    //         addUser(newUser);
    //         resetForm();
    //     }
    // };

    const resetForm = () => {
        setNewUser({
            firstName: '',
            lastName: '',
            email: '',
            date: '',
            username: '',
            profilePic: '',
            password: '',
            Password_Confirm: ''
        });
        setError('');
        setImagePreview(null);
        navigate('/Login');
    };



    return (
        <div className={` center ${darkMode ? 'dark-mode1' : ''}`} >
            <div className='container'>
                <div className="col-4">
                    <div className="logo">
                        <img
                            src="youtube.svg" viewbox="0 0 48 48" width={48} height={48}>
                        </img>
                        <h2> Create a UTube Account</h2>
                        <h5> enter your information</h5>
                        {imagePreview && (
                            <div className="pre">
                                <div>image preview</div>
                                &nbsp;
                                <img src={imagePreview} alt="Profile Preview" style={{ width: '100px', height: '100px' }} />
                            </div>
                        )}
                    </div>
                </div>
                <div className="SignUp">
                    <form className="row g-2 form2" onSubmit={handleSubmit} >
                        <div className="col-md-6">
                            <label for="validationDefault01" className="form-label">First name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="validationCustom01"
                                placeholder="First Name"
                                value={newUser.firstName}
                                onChange={handleChange}
                                name="firstName"
                                required
                            />
                        </div>
                        <div className="col-md-6">
                            <label for="validationDefault01" className="form-label">Last name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="validationCustom02"
                                placeholder="Last Name"
                                value={newUser.lastName}
                                onChange={handleChange}
                                name="lastName"
                                required
                            />
                        </div>
                        <div className="col-md-6">
                            <label htmlFor="validationCustom03" className="form-label">date of Birth</label>
                            <input
                                type="date"
                                className="form-control"
                                id="validationCustom03"
                                placeholder="date of Birth"
                                value={newUser.date}
                                onChange={handleChange}
                                name="date"
                                required
                            />
                            <div className="invalid-feedback">
                                Please provide a valid date of birth.
                            </div>
                        </div>
                        <div className="col-md-6">
                            <label htmlFor="validationCustom04" className="form-label">email</label>
                            <input
                                type="email"
                                className="form-control"
                                id="validationCustom04"
                                placeholder="email"
                                value={newUser.email}
                                onChange={handleChange}
                                name="email"
                                required
                            />
                        </div>
                        <div className="col-md-6">
                            <label for="validationCustom05" className="form-label">Profile Picture</label>
                            <input
                                type="file"
                                className="form-control"
                                id="validationCustom05"
                                name="profilePic"
                                accept="image/*"
                                onChange={handleChange}
                                required
                            />
                            <div className="invalid-feedback">
                                Please upload a valid image file.
                            </div>
                        </div>
                        <div className="col-md-6">
                            <label for="validationDefaultUsername" className="form-label">Username</label>
                            <input
                                type="text"
                                className="form-control"
                                id="validationCustom06"
                                placeholder="Username"
                                value={newUser.username}
                                onChange={handleChange}
                                name="username"
                                required
                            />
                        </div>
                        <div className="col-md-6">
                            <label htmlFor="validationCustom07" className="form-label">Password</label>
                            <input
                                type="password"
                                className="form-control"
                                id="validationCustom07"
                                placeholder="Password"
                                value={newUser.password}
                                onChange={handleChange}
                                name="password"
                                required
                            />
                            <div className="invalid-feedback">
                                Please provide a password.
                            </div>
                        </div>
                        <div className="col-md-6">
                            <label htmlFor="validationCustom08" className="form-label">Confirm Password</label>
                            <input
                                type="password"
                                className="form-control"
                                id="validationCustom08"
                                placeholder="Confirm Password"
                                value={newUser.Password_Confirm}
                                onChange={handleChange}
                                name="Password_Confirm"
                                required
                            />
                            <div className="invalid-feedback">
                                Please confirm your password.
                            </div>
                        </div>
                        <button type="submit" className="btn btn-outline-danger">Sign Up</button>
                        <div className='error'>
                            {error && <p className="error">{error}</p>}
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default SignUp;


