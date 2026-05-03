# UTube - Video Sharing Platform

## Project Overview
UTube is a full-featured video-sharing application modeled after YouTube, developed as part of an Advanced System Programming course at Bar-Ilan University. The project features:

- Dual-server architecture with a primary Node.js server for core functionality and a C++ server for intelligent content recommendations
- Fully developed client-side applications for web (React) and Android platforms

This project is split into 4 main branches:
1. C++ server (current branch)
2. Node.js server 
3. Web client-side (React)
4. Android client-side

## Wiki - Full Documentation
**For complete details, explanations, screenshots, and usage instructions, please visit the [Wiki tab](https://github.com/Yedpel/UTube-VideoPlatform/wiki) in the toolbar at the top of this GitHub page.**

The wiki contains comprehensive information about all components of the UTube project, including setup guides, API documentation, and user manuals.

This branch contains the c++ server component of the UTube project. Below, you'll find a brief overview and setup instructions specific to this component.

---

# UTUBE C++ SERVER

## OVERVIEW
Welcome to the UTube C++ Server branch, a key component of the UTube app developed as part of an Advanced System Programming course at Bar-Ilan University. This repository holds the server-side code that powers our UTube application, which simulates a simplified version of a video streaming service where users can watch, like, and comment on videos.

## FULL PROJECT DETAILS
For full details on the entire UTube project, including the Android app, React web app, and Node.js server, please skip this README.md and refer to the wiki pages located inside the `cpp_server` branch's wiki folder or on the wiki page of the entire repository.

## GETTING STARTED
To set up and run the server locally, follow these steps:

### Step 1: Install the required JSON library
Before compiling, run this command in the terminal to install the required JSON library:

```bash
sudo apt-get install nlohmann-json3-dev
```

### Step 2: Compile and run the server
To compile and run the C++ server, use this command in the terminal:

```bash
make && make run
```

To stop the server, use `Ctrl+C`.

### Additional options:
- `make` - Compile only
- `make run` - Run the compiled server
- `make clean` - Remove compiled files

## IMPORTANT: PORT AVAILABILITY CHECK
Before running the services, ensure that port `55551` is available on your system. Follow these steps to check:

### Windows:
1. Open Command Prompt (cmd).
2. Run the following command:

```bash
netstat -ano | findstr :55551
```

If there's no output, the port is available.

### Linux/macOS:
1. Open Terminal.
2. Run the following command:

```bash
lsof -i :55551
```

If there's no output, the port is available.

If the port is in use, you may need to choose a different port or close the application using it.

### Note: 
If you change the port, remember to update it in both:
1. The **C++ server** in `main.cpp` at the line on top:

```cpp
const int SERVER_PORT = 55551;
```

2. The **NodeJS server** in `services/cppServerService.js` at the line on top:

```js
const CPP_SERVER_PORT = 55551;
```
