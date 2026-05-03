Welcome to the UTube wiki!

1. Project Overview
The UTube project is a multi-platform video-sharing application developed as part
 of the Advanced System Programming course at Bar-Ilan University. 
The project was developed by Yedidya Peles, Shimon Rahamim, and Avraham Bicha, 
and it provides a fully functional video streaming service. Key features 
include user authentication, video upload, viewing, commenting, and personalized recommendations.

2. Architecture Overview
The UTube project is divided into several components:
*   C++ Server: Handles TCP communication and multithreading 
    for client requests. It processes requests from the Node.js
    server and returns video recommendations.

*  Node.js Server: Manages user authentication, video data, and
   interaction with the C++ server. It acts as a middleman between 
   the clients (React web app and Android app) and the C++ server.

*  React Web App: A client-side application for browsing, uploading, 
   and viewing videos. It communicates with the Node.js server to fetch 
   data and send user actions.

*  Android App: A mobile client offering similar functionality to the 
   React web app, with additional mobile-specific features.


3. Setup and Installation
## Cloning the Repositories
Clone the repositories for each component of the project:
1. UTube Node.js Server:
   #### git clone https://github.com/[username]/UTube-Server.git
2. UTube C++ Server:
   ##### git clone https://github.com/[username]/UTube-CPP-Server.git
3. UTube Android App:
   #### git clone https://github.com/Yedpel/UTube_Android.git

# Guidence:
You must follow the setup in the exact order 

## Setting Up the C++ Server
1. Install Dependencies: Ensure you have the required JSON library installed:
   #### sudo apt-get install nlohmann-json3-dev
2. Compile and Run the Server: Navigate to the server directory and run:
   #### make run
## c++ server running
![2024-09-02](https://github.com/user-attachments/assets/82110a19-b4cc-4ea3-9595-61bf8a2f6e6c)

## Setting Up the Node.js Server
1. Install Dependencies: Navigate to the Node.js server directory and run:
   #### npm install
2. Run the Node.js Server:
   #### npm start
## Node.js server running
![2024-09-02](https://github.com/user-attachments/assets/592f4010-3a8b-4d85-82c1-6d5cd360ff85)


## Running the Web App in Your Browser:
  * Open your web browser 
  * Enter the following URL in the address bar: http://localhost:12345/ 

## UTube Web homepage
![2024-09-02 (2)](https://github.com/user-attachments/assets/a2dea178-887e-40fe-813d-1488cb186d05)


## Running the Android App
1. Open in Android Studio: Open Android Studio and select "Open an Existing Project," navigating
   to the cloned Android project.
2. Run the Application:Choose an emulator or connect a physical device, then click the "Run" 
   button in Android Studio.
## UTube Android app homepage
![image](https://github.com/user-attachments/assets/370042a9-646c-4aee-8fe3-3ab4ab1ec1e6)



4. Features and Functionality
* User Authentication: Users can sign up, log in, and manage their accounts.

* Video Upload: Users can upload videos that are stored and managed on the server.

* Video Viewing: Users can view videos, with the option to comment and like.

* Recommendations: The C++ server provides personalized video recommendations based on user activity.

5. Web  app experience
The web app experience is doucomanted in README (2) file in this folder.

6. Android App experience
The Android app experience is doucomanted in README (3) file in this folder.

7. Testing and Debugging
# C++ Server:
* Logging: Use logging to trace request handling and thread management.
# Node.js Server:
* Console Logs: Utilize console.log() for debugging server requests and interactions with the C++ server.
# Android App:
* Logcat: Use Android Studio's Logcat to debug the app's behavior and network requests.

8. Troubleshooting
* C++ Server Bind Error: If you encounter a bind error, 
remove using namespace std; and add std:: where needed.
* Node.js Server Connection Issues: Ensure MongoDB is running 
and the connection string in the .env file is correct.
* Android App Server Connection: Verify that the BASE_URL in ApiClient.kt
 is correct and that your device is on the same network as the server.

9. Contribution Guidelines
We welcome contributions to improve the UTube project. 
Feel free to fork the repositories, make your changes, and submit a pull request.
Please ensure your code follows the project's coding standards and includes tests for any new features.

© 2024 Yedidya Peles, Shimon Rahamim, Avrham Bicha. All Rights Reserved.

