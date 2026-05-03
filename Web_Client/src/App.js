import './App.css';
import Login from './login/Login';
import SignUp from './signUp/SignUp';
import User from './user/User';
import { UserProvider } from './UserContext';
import HomePage from './homepage/HomePage';
import Watch from './watch/Watch';
import React from 'react';
// import videos from './data/Videos.json';
import { BrowserRouter,Route,Routes } from 'react-router-dom';
import { useState } from 'react';
import './DarkMode.css';
// import React from 'react';
// import ReactDOM from 'react-dom';


function App() {
  const [userLoggedIn,setUserLoggedIn] = useState(null);
  const [darkMode,setDarkMode] = useState(false);

  return (
    <UserProvider>
      <div className="App ">
        <BrowserRouter>
          <Routes>
            <Route path='/' element={<HomePage   darkMode={darkMode} setDarkMode={setDarkMode} />} ></Route>
            <Route path='/users/:id' element={<User  darkMode={darkMode} userLoggedIn={userLoggedIn}/>} />
            <Route path='/users/:id/videos/:pid' element={<Watch darkMode={darkMode} />} />
            <Route path="/login" element={<Login  darkMode={darkMode}/>} ></Route> 
            <Route path="/signUp" element={<SignUp darkMode={darkMode}  />} ></Route>
          </Routes>
        </BrowserRouter>
      </div>
    </UserProvider>
  );  
}

export default App;


// <Route path='/' element={<HomePage  userLoggedIn={userLoggedIn} setUserLoggedIn = {setUserLoggedIn} videosList={videosList} setVideosList={setVideosList} darkMode={darkMode} setDarkMode={setDarkMode} />} ></Route>
// <Route path='/user' element={<User videosList={videosList} darkMode={darkMode} userLoggedIn={userLoggedIn}/>} />
// <Route path='/watch' element={<Watch  userLoggedIn={userLoggedIn} setUserLoggedIn = {setUserLoggedIn} videosList={videosList} setVideosList={setVideosList} darkMode={darkMode} />} />
