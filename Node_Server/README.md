# UTube - Video Sharing Platform

## Project Overview
UTube is a full-featured video-sharing application modeled after YouTube, developed as part of an Advanced System Programming course at Bar-Ilan University. The project features:

- Dual-server architecture with a primary Node.js server for core functionality and a C++ server for intelligent content recommendations
- Fully developed client-side applications for web (React) and Android platforms

This project is split into 4 main branches:
1. Node.js server (current branch)
2. C++ server
3. Web client-side (React)
4. Android client-side

## Wiki - Full Documentation
**For complete details, explanations, screenshots, and usage instructions, please visit the [Wiki tab](https://github.com/Yedpel/UTube-VideoPlatform/wiki) in the toolbar at the top of this GitHub page.**

The wiki contains comprehensive information about all components of the UTube project, including setup guides, API documentation, and user manuals.

This branch contains the Node.js server component of the UTube project. Below, you'll find a brief overview and setup instructions specific to this component.

---

# UTube Node.js Server

## Overview
Welcome to the UTube Node.js Server branch, a key component of the UTube app developed as part of an Advanced System Programming course at Bar-Ilan University. This repository holds the server-side code that powers our UTube application, which simulates a simplified version of a video streaming service where users can watch, like, and comment on videos.

## Full Project Details
For full details on the entire UTube project, including the Android app, React web app, and C++ server, please skip this README.md and refer to the wiki pages located inside the nodeJS_server branch's wiki folder or on the wiki page of the entire repository.

### Features
The server handles a variety of functions including:
- User authentication and management
- Video uploads, updates, and fetching
- Comment system with support for likes
- Video recommendations based on views and categories

## Getting Started
To set up and run the server locally, follow these steps:

1. **Clone the repository**
   First, clone the repository and switch to the correct branch:
   ```bash
   git clone https://github.com/[username]/task4_UTube.git
   cd task4_UTube
   git checkout NodeJs_Server
3. **Install dependencies**
Ensure that you have Node.js installed on your system. Then run:
   ```bash
   npm install
   npm i express
    
5. Run the application
Start the server using:
   ```bash
    npm start

Contribution
This project benefits from the collective efforts of our team, combining diverse ideas and coding practices to create a functional and engaging application.

Feel free to explore the code and suggest improvements or enhancements by submitting pull requests or opening issues.

Thank you for visiting our project!

---
© 2024 Yedidya Peles, Shimon Rahamim, Avrham Bicha. All Rights Reserved.

