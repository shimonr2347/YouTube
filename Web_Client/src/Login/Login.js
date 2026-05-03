import React, { useContext, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserContext } from '../UserContext';
import './Login.css';
// import youtube from './youtube.svg';
import axios from 'axios';


function Login({  darkMode }) {
  
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    // const token = localStorage.getItem('token');

    const getUserDetails = async (id) => {
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.error('No token found');
            return;
        }
        try {
            const response = await axios.get(`http://localhost:12345/api/users/${id}`,{
                headers: {
                    Authorization: `Bearer ${token}`,
                }
            });
            if (response.statusText === "OK") {
                const user = await response.data;
                sessionStorage.setItem('userName', user.username);
                sessionStorage.setItem('profilePic', user.profilePic);
                navigate('/');
            } else {
                console.error('failed to connect the server');
            }
        } catch (error) {
            console.error('Failed to fetch user:', error);
        }
    };


    const createThread = async (id) => {
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.error('No token found');
            return;
        }
        try {
            const response = await axios.post(`http://localhost:12345/api/cpp/create-thread/`,
                {},
                {
                headers: {
                    Authorization: `Bearer ${token}`,
                }
            });
            if (response.statusText === "OK") {
                getUserDetails(id);
            } else {
                console.error('failed to connect the server');
            }
        } catch (error) {
            console.error('Failed to fetch user:', error);
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const response = await fetch('http://localhost:12345/api/tokens/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });
            if (response.ok) {
                const data = await response.json();
                sessionStorage.setItem('token', data.token);
                sessionStorage.setItem('userId', data.userId);
                console.log('Login successful and the token is:', data.token);
                createThread(data.userId);
            } else {
                const errorData = await response.json();
                console.log('Login failed:', errorData.message);
                setError( 'Login failed. Please try again.' );
            }

        } catch (error) {
            console.error('Error during authentication:', error);
            setError('An error occurred. Please try again.');
        }
    };



    // const handleSubmit = (event) => {
    //     event.preventDefault();
    //     const user = userList.find(
    //         (user) => user.User_name === username && user.Password === password
    //     );
    //     if (user) {
    //         setAuth(user);
    //         console.log('Login successful');
    //         setUserLoggedIn(user);
    //         navigate('/'); // Navigate to the home page or dashboard after login
    //     } else {
    //         console.log('Invalid credentials');
    //         setError('Invalid credentials.');
    //         return;
    //     }
    // };

    return (
        <div className={` center ${darkMode ? 'dark-mode1' : ''}`}>
            <div className={"container1 "}>
                <div className="col-4">
                    <div className="logo">
                        <img src="youtube.svg" alt="app logo"  viewbox="0 0 48 48" width={48} height={48}>
                        </img>
                        <h1> Sign in</h1>
                        <h5> to continiue to UTube</h5>
                    </div>
                </div>
                <div className="col-8">
                    <div className="login">
                        <form className="row g-3 form2 " onSubmit={handleSubmit} >
                            <div className="first">
                                <input
                                    name="userName"
                                    type="text"
                                    className="form-control mb-1"
                                    placeholder="User name"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                />
                                <label className="visually-hidden">Password</label>
                                <input
                                    name="password"
                                    type="password"
                                    className="form-control"
                                    placeholder="Password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                            </div>
                            <div className="second">
                                <Link to='/signUp'>
                                    <button
                                        type="button"
                                        className="btn btn-outline-danger">Create account</button>
                                </Link>
                                {error && <p className="error">{error}</p>}
                                <button type="submit" className="btn btn-primary">Sign in</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

    );
}

export default Login;