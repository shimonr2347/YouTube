import express from 'express';
import { upload } from './mediaRoutes.js';
import { updateUser, deleteUser, registerUser, getUserSelectedDetails, getUserFullDetails } from '../controllers/users.js';
//import {isLoggedIn} from '../controllers/login.js';
import { isLoggedIn } from '../middlewares/auth.js';
import { checkUserNameAndPassword } from '../controllers/login.js';

const router = express.Router();

// Route to create user with profile picture
router.post('/users', upload.single('profilePic'), registerUser);

router.get('/users/:id',isLoggedIn, getUserSelectedDetails);

router.get('/users/:id/page', getUserFullDetails);

// Route to update a user with new profile picture
router.put('/users/:id', isLoggedIn, upload.single('profilePic'), updateUser);

// Route to delete a user
router.delete('/users/:id',isLoggedIn, deleteUser);

export default router;




