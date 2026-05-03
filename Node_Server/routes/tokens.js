import express from 'express'
import { checkUserNameAndPassword } from '../controllers/login.js';
const router = express.Router();

//create token to authenticate user
router.post('/tokens', checkUserNameAndPassword);

export default router



