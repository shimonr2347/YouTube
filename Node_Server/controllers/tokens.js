import { getUserid } from '../services/users.js';
import jwt from 'jsonwebtoken';
const key = "secretkey";

export const createToken = async (req, res) => {
    const { username} = req.body;
    try {
        const id = await getUserid(username);
        if (id) {
            // Generate JWT token
            const token = jwt.sign(username, key);
            res.status(200).json({
                message: 'Login successful',
                token: token,
                userId: id
            });
        } else {
            res.status(404).json({ message: 'Invalid username or password' });
        }
    } catch (err) {
        console.error('Error during login:', err);
        res.status(500).json({ message: 'Internal server error' });
    }
};