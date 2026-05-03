import jwt from 'jsonwebtoken';
import * as userService from '../services/users.js';
const key = "secretkey";  

export async function isLoggedIn(req, res, next) {
    const token = req.headers.authorization?.split(' ')[1];
    if (!token) {
        console.log('Token required - no token found in headers');
        return res.status(401).json({ message: 'Token required' });
    }
    console.log('token', token);
    console.log('key', key);
    try {
        const decoded = jwt.verify(token, key);
        const user = await userService.getUserbyUsername(decoded.username);
        console.log('user', user.username);
        if (!user) {
            console.error('User not found in DB');
            return res.status(401).json({ message: 'Unauthorized' });
        }
        req.user = user; // Add the user object to the request for downstream use
        next();
    } catch (err) {
        console.error('Error verifying token:', err);
        res.status(401).json({ message: 'Unauthorized, invalid token' });
    }
}
