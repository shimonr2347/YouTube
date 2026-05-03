#include "request_handler.hpp"
#include "user_thread_manager.hpp"
#include "video_manager.hpp"
#include <nlohmann/json.hpp>
#include <iostream>
#include <sstream>
#include <unistd.h>
#include <sys/socket.h>
#include <thread>

using json = nlohmann::json;

void handleClient(int clientSocket)
{
    char buffer[4096];
    int bytesRead = recv(clientSocket, buffer, sizeof(buffer) - 1, 0);

    if (bytesRead <= 0)
    {
        std::cerr << "Error reading from socket or connection closed" << std::endl;
        return;
    }

    buffer[bytesRead] = '\0';
    std::cout << "Received: " << buffer << std::endl;

    // Extract JSON body from the received data
    std::string request(buffer);
    size_t pos = request.find("\r\n\r\n");
    std::string jsonBody;
    if (pos != std::string::npos)
    {
        jsonBody = request.substr(pos + 4);
    }
    else
    {
        jsonBody = request; // Assume the entire request is JSON if no headers are found
    }

    std::cout << "Extracted JSON body: " << jsonBody << std::endl;

    try
    {
        json data = json::parse(jsonBody);
        std::string action = data["action"];
        std::string userId = data["userId"];

        json response;

        if (action == "create_thread")
        {
            std::string threadId = UserThreadManager::getInstance().createThreadForUser(userId);
            response["message"] = "Thread created successfully";
            response["threadId"] = threadId;
        }
        else if (action == "close_thread")
        {
            std::string threadId = UserThreadManager::getInstance().closeThreadForUser(userId);
            response["message"] = "Thread closed successfully";
            response["threadId"] = threadId;
        }
        else if (action == "notify-watch")
        {
            if (!data.contains("videoId"))
            {
                throw std::runtime_error("Missing videoId for notify-watch action");
            }
            std::string videoId = data["videoId"];

            if (!UserThreadManager::getInstance().hasThreadForUser(userId))
            {
                UserThreadManager::getInstance().createThreadForUser(userId);
            }

            std::future<std::string> futureThreadId = UserThreadManager::getInstance().processUserRequest(userId, action, videoId);
            std::string threadId = futureThreadId.get();
            response["message"] = "Video watch recorded";
            response["threadId"] = threadId;
        }
        else if (action == "get_recommendations")
        {
            if (!data.contains("videoId") || !data.contains("allVideos"))
            {
                throw std::runtime_error("Missing videoId or allVideos for get_recommendations action");
            }
            std::string videoId = data["videoId"];

            // Update video data
            std::unordered_map<std::string, int> newVideoData;
            for (const auto &video : data["allVideos"])
            {
                newVideoData[video["id"]] = video["views"];
            }
            VideoManager::getInstance().updateVideoData(newVideoData);

            // Process request in user's thread (or guest thread)
            std::future<std::string> futureThreadId;
            if (userId == "guest")
            {
                futureThreadId = UserThreadManager::getInstance().processGuestRequest(action, videoId);
            }
            else
            {
                futureThreadId = UserThreadManager::getInstance().processUserRequest(userId, action, videoId);
            }
            std::string threadId = futureThreadId.get();

            // Generate recommendations
            std::vector<std::string> recommendations = VideoManager::getInstance().getRecommendedVideos(videoId, userId, 6);

            response["message"] = "Recommendations generated successfully";
            response["threadId"] = threadId;
            response["recommendations"] = recommendations;
        }
        else
        {
            throw std::runtime_error("Unknown action: " + action);
        }

        std::string responseStr = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + response.dump();
        send(clientSocket, responseStr.c_str(), responseStr.length(), 0);
    }
    catch (const json::exception &e)
    {
        std::cerr << "JSON parsing error: " << e.what() << std::endl;
        std::string response = "HTTP/1.1 400 Bad Request\r\nContent-Type: application/json\r\n\r\n";
        response += "{\"error\":\"Invalid JSON: " + std::string(e.what()) + "\"}";
        send(clientSocket, response.c_str(), response.length(), 0);
    }
    catch (const std::exception &e)
    {
        std::cerr << "Error processing request: " << e.what() << std::endl;
        std::string response = "HTTP/1.1 400 Bad Request\r\nContent-Type: application/json\r\n\r\n";
        response += "{\"error\":\"Invalid request: " + std::string(e.what()) + "\"}";
        send(clientSocket, response.c_str(), response.length(), 0);
    }

    close(clientSocket);
}