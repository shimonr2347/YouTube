## UTube web client side
Full Project Details
For full details on the entire UTube project, including the backend servers and Android app, please skip this README.md and refer to the wiki pages located in the NodeJs_Server branch under the wiki folder.

This branch is intended only for editing the design or adding features to the web app. To launch the full project, the server-side configuration is necessary, and the details for setting it up are found in the wiki.

## Overview
Creating the UTube web client was a rewarding and challenging experience. Our group collaborated by brainstorming ideas and sketching out designs. Avraham worked on the home page and video management features, while Shimon focused on login and signup screens. We overcame various debugging issues and learned a lot about teamwork throughout the process.

1. **Clone the Repository**
   ```bash
    git clone https://github.com/Yedpel/task4_UTube.git
    cd task4_UTube
    cd web_react

2. **Run the Application**
Use the following command to run the app in development mode:
   ```bash
     npm start

This will open http://localhost:3000 to view it in your browser. The page will automatically reload when you make edits, and any lint errors will show in the console.

 3. **Testing the Application**
To run the tests interactively, use:
    ```bash 
      npm test

You can refer to the Create React App documentation for more details about running tests.

 4. **Building for Production**
To build the web app for production:
    ```bash 
     npm run build

This will bundle and optimize the app into the build folder. After making design changes or adding features, you need to run this command. The files and folders from the build folder, except the media folder, should be copied to replace the current files in the public folder of the server-side code.
The media folder in the server’s public directory should remain unchanged.
