import * as userService from '../services/users.js';
import jwt from 'jsonwebtoken';
const key = "secretkey"; // Ensure this key is stored securely and consistently


export const registerUser = async (req, res) => {
    const { firstName, lastName, date, email, username, password } = req.body;
    const profilePic = req.file ? `/media/${req.file.filename}` : '/media/Profile_Images/ic_profile_placeholder.webp';

    try {
        const usernameExists = await userService.findUser(username);
        if (usernameExists) {
            return res.status(400).json({ message: 'Username is already in use' });
        }

        const newUser = { firstName, lastName, date, email, profilePic, username, password };
        const createdUser = await userService.createUser(newUser);
        res.status(201).json({ message: 'User registered successfully' });
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
};

export const getUserFullDetails = async (req, res) => {
    // console.log(req.params.id)
    ;
    try {
        const user = await userService.getUserbyId(req.params.id);
        if (user !== null) {
           console.log('login successful the user is :', user.username);
            res.json(user);
        } else {
            res.status(404).send('User not found');
            console.log('login not successful');
        }
    } catch (error) {
        console.log('login failed');
        res.status(500).send('Error fetching user');
    }
}

export const getUserSelectedDetails = async (req, res) => {
    ;
    try {
        const user = await userService.getUserSelectedDetails(req.params.id);
        if (user !== null) {
           console.log('login successful the user is :', user.username);
            res.json(user);
        } else {
            res.status(404).send('User not found');
            console.log('login not successful');
        }
    } catch (error) {
        console.log('login failed');
        res.status(500).send('Error fetching user');
    }
}

export async function deleteUser(req, res) {
    console.log('delete user');
    const user = await userService.getUserbyId(req.params.id);
    const userToDelete = user.username;
    console.log(userToDelete);
    try {
        const deletedUser = await userService.deleteUserModel(req.params.id);
        if (deletedUser) {
            res.send(`The User: ${userToDelete} deleted successfully`);
        } else {
            res.status(404).send('User not found');
        }
    } catch (error) {
        res.status(500).send('Failed to delete user');
    }
}


export async function updateUser(req, res) {
    console.log('update user');
    const userId = req.params.id;
    const oldUser = await userService.getUserbyId(userId);
  
    try {
      // If the username from the request is different from the username in the database
      if (oldUser.username !== req.body.username) {
        // If the username changed, check if the new username is already in use
        const usernameExists = await userService.findUser(req.body.username);
        if (usernameExists) {
          return res.status(400).json({ message: 'New username is already in use' });
        }
      }
  
      const updateData = {
        ...req.body,
        profilePic: req.file ? `/media/${req.file.filename}` : oldUser.profilePic,
      };
  
      const updatedUser = await userService.updateUserModel(userId, updateData);
  
      if (updatedUser) {
        // Issue a new token if username was part of the update
        if (req.body.username) {
          const newToken = jwt.sign({ username: updatedUser.username }, key, { expiresIn: '5h' });
          res.json({ message: 'User updated successfully', token: newToken , username: updatedUser.username, profilePic: updatedUser.profilePic});
        } else {
          res.json({ message: 'User updated successfully', profilePic: updatedUser.profilePic });
        }
      } else {
        res.status(404).send('User not found');
      }
    } catch (error) {
      res.status(500).send('Failed to update user');
    }
  }


