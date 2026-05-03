import express from 'express';
import multer from 'multer';
import fs from 'fs';
import path from 'path';

const router = express.Router();

const storage = multer.diskStorage({
    destination(req, file, cb) {
        cb(null, 'public/media/');
    },
    filename(req, file, cb) {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
    }
});

// Specify allowed file types
const fileFilter = (req, file, cb) => {
    const allowedTypes = /jpeg|jpg|png|gif|webp|mp4|mov|avi/;  // Add more video types if necessary
    if (!file.originalname.match(`\.(${allowedTypes.source})$`)) {
        return cb(new Error(`Only image and video files are allowed! (jpeg, jpg, png, gif, webp for images and mp4, mov, avi for videos)`), false);
    }
    cb(null, true);
};

// Setup the multer upload with the defined storage, file filter, and size limit
export const upload = multer({ 
    storage: storage,
    fileFilter: fileFilter,
    limits: { fileSize: 10 * 1024 * 1024 }  // 10 MB size limit
});

// upload with error handling for file size and other errors
router.post('/upload', upload.single('file'), (req, res) => {
    if (req.file) {
        res.json({ message: 'File uploaded successfully', filePath: `/media/${req.file.filename}` });
    }
}, (error, req, res, next) => {
    if (error.code === 'LIMIT_FILE_SIZE') {
        res.status(413).json({ message: 'File is too large. Please upload files less than 10MB.' });
    } else {
        res.status(500).json({ message: 'Error uploading file' });
    }
});


router.post('/replace', upload.single('file'), (req, res) => {
    const oldFilePath = req.body.oldFilePath;
    fs.unlink(`public${oldFilePath}`, (err) => {
        if (err) {
            return res.status(500).json({ message: 'Failed to delete the old file' });
        }
        res.json({ message: 'File replaced successfully', newFilePath: `/media/${req.file.filename}` });
    });
});

export default router;
