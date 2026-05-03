# UTUBE_Android

Welcome to the UTube Android branch, a key component of the UTube app developed as part of an Advanced System Programming course at Bar-Ilan University. This repository holds the server-side code that powers our UTube application, which simulates a simplified version of a video streaming service where users can watch, like, and comment on videos.

## Full Project Details
For full details on the entire UTube project, including the Android app, React web app, and C++ server, please skip this README.md and refer to the wiki pages located inside the nodeJS_server branch's wiki folder or on the wiki page of the entire repository.

## Getting Started

Again - full explain is on the wiki.

These instructions will get you a copy of the project up and running on your local machine for
development and testing purposes. Follow these simple steps to get started.

### Prerequisites

1. Ensure you have Android Studio installed on your computer. If not, download and install it
   from [Android Studio's official website](https://developer.android.com/studio).
2. Make sure you have the UTUBE-Server barnch server_task3 set up and running. If not, follow the
   instructions in the server's README to set it up.

### Setting up the Project

1. **Clone the Repository**
   Open your terminal and run the following command:
   ```bash
   git clone https://github.com/Yedpel/android_task_4
   cd UTube_Android

2. **Open the Project in Android Studio**
    - Open Android Studio. On the welcome screen, select Open an Existing Project or
      go to File > Open... if you have another project open.
    - Navigate to the directory where you cloned the project and select it.


3. **Run the Application**
    - After the project opens, let Android Studio build the project. If there are any dependencies
      to be downloaded, Android Studio will manage this automatically.

    - To run the app, choose an emulator or connect an Android device to your computer.

    - Click on the Run button (green triangle) in the toolbar. Android Studio will build
      the application and install it on the selected device or emulator.

**Running the Project with the rest of the other beanches - full explain on the wiki**


**Troubleshooting**

- If you encounter any issues with building or running the app,ensure your Android SDK is up-to-date
  and that you have the correct build tools installed. Check the build.gradle file for any specific
  SDK or library dependency that might need attention.
- If the app cannot connect to the server, double-check that the server is running
  and that the BASE_URL in the app's configuration is correct. Also, ensure that your firewall
  is not blocking the connection.
- For any server-related issues, refer to the UTUBE-Server README for troubleshooting steps.
  **Contributing**
  We welcome contributions to improve the UTUBE Android app. Feel free to fork the repository,
  make your changes, and submit a pull request.

---
© 2024 Yedidya Peles, Shimon Rahamim, Avrham Bicha. All Rights Reserved.
