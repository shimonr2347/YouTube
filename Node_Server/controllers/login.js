import * as userService from '../services/users.js';
import jwt from 'jsonwebtoken'
const key = "secretkey"
import bcrypt from 'bcryptjs';


export const checkUserNameAndPassword = async (req, res) => {
    const { username, password } = req.body;
  
    try {
        const user = await userService.getUserbyUsername(username);
        const match = password===user.password;

        if (user &&  match) { 
            const token = jwt.sign({ username }, key, { expiresIn: '5h' });

            res.status(200).json({ message: 'Login successful', token, userId: user.id});
        } else {
            console.log('Invalid username or password');
            res.status(401).json({ message: 'Invalid username or password' });
        }
    } catch (err) {
        console.error('Login error:', err.message); 
        res.status(500).json({ message: 'Internal server error' });
    }
};
