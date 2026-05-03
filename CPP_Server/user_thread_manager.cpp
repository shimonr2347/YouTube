#include "user_thread_manager.hpp"
#include "video_manager.hpp"
#include <iostream>
#include <thread>
#include <sstream>

UserThreadManager::UserThreadManager() : guestThreadActive(true)
{
    guestThread = std::thread(&UserThreadManager::handleGuestRequests, this);
}

UserThreadManager::~UserThreadManager()
{
    guestThreadActive = false;
    guestCondVar.notify_one();
    if (guestThread.joinable())
    {
        guestThread.join();
    }
}

UserThreadManager &UserThreadManager::getInstance()
{
    static UserThreadManager instance;
    return instance;
}

std::string UserThreadManager::createThreadForUser(const std::string &userId)
{
    std::lock_guard<std::mutex> lock(threadMapMutex);
    if (userThreads.find(userId) == userThreads.end())
    {
        userThreadActive[userId] = true;
        userThreads[userId] = std::thread(&UserThreadManager::handleUserRequests, this, userId);
        std::thread::id threadId = userThreads[userId].get_id();
        std::stringstream ss;
        ss << threadId;
        userThreadIds[userId] = ss.str();
        std::cout << "Thread created for user: " << userId << " with ID: " << userThreadIds[userId] << std::endl;
    }
    return userThreadIds[userId];
}

std::string UserThreadManager::closeThreadForUser(const std::string &userId)
{
    std::lock_guard<std::mutex> lock(threadMapMutex);
    auto it = userThreads.find(userId);
    if (it != userThreads.end())
    {
        std::string threadId = userThreadIds[userId];
        userThreadActive[userId] = false;
        userCondVars[userId].notify_one();
        if (it->second.joinable())
        {
            it->second.join();
        }
        userThreads.erase(it);
        userMessageQueues.erase(userId);
        userMutexes.erase(userId);
        userCondVars.erase(userId);
        userThreadActive.erase(userId);
        userThreadIds.erase(userId);
        std::cout << "Thread closed for user: " << userId << " with ID: " << threadId << std::endl;
        return threadId;
    }
    return "";
}

bool UserThreadManager::hasThreadForUser(const std::string &userId)
{
    std::lock_guard<std::mutex> lock(threadMapMutex);
    return userThreads.find(userId) != userThreads.end();
}

std::future<std::string> UserThreadManager::processUserRequest(const std::string &userId, const std::string &action, const std::string &videoId)
{
    std::lock_guard<std::mutex> lock(userMutexes[userId]);
    UserMessage message{action, videoId, std::promise<std::string>()};
    std::future<std::string> futureThreadId = message.threadIdPromise.get_future();
    userMessageQueues[userId].push(std::move(message));
    userCondVars[userId].notify_one();
    return futureThreadId;
}

std::string UserThreadManager::getThreadIdForUser(const std::string &userId)
{
    std::lock_guard<std::mutex> lock(threadMapMutex);
    auto it = userThreadIds.find(userId);
    if (it != userThreadIds.end())
    {
        return it->second;
    }
    return "";
}

void UserThreadManager::handleUserRequests(const std::string &userId)
{
    std::thread::id this_id = std::this_thread::get_id();
    std::stringstream ss;
    ss << this_id;
    std::string thread_id = ss.str();

    std::cout << "Thread " << thread_id << " started for user: " << userId << std::endl;

    while (userThreadActive[userId])
    {
        std::unique_lock<std::mutex> lock(userMutexes[userId]);
        userCondVars[userId].wait(lock, [this, &userId]
                                  { return !userMessageQueues[userId].empty() || !userThreadActive[userId]; });

        if (!userThreadActive[userId])
            break;

        auto message = std::move(userMessageQueues[userId].front());
        userMessageQueues[userId].pop();
        lock.unlock();

        if (message.action == "notify-watch")
        {
            VideoManager::getInstance().addViewer(message.videoId, userId);
            std::cout << "Thread " << thread_id << " - User " << userId << " watched video " << message.videoId << std::endl;
        }
        // Add other actions here as needed

        message.threadIdPromise.set_value(thread_id);
    }

    std::cout << "Thread " << thread_id << " ended for user: " << userId << std::endl;
}

std::future<std::string> UserThreadManager::processGuestRequest(const std::string &action, const std::string &videoId)
{
    std::lock_guard<std::mutex> lock(guestMutex);
    UserMessage message{action, videoId, std::promise<std::string>()};
    std::future<std::string> futureThreadId = message.threadIdPromise.get_future();
    guestMessageQueue.push(std::move(message));
    guestCondVar.notify_one();
    return futureThreadId;
}

void UserThreadManager::handleGuestRequests()
{
    std::thread::id this_id = std::this_thread::get_id();
    std::stringstream ss;
    ss << this_id;
    std::string thread_id = ss.str();

    std::cout << "Guest thread " << thread_id << " started" << std::endl;

    while (guestThreadActive)
    {
        std::unique_lock<std::mutex> lock(guestMutex);
        guestCondVar.wait(lock, [this]
                          { return !guestMessageQueue.empty() || !guestThreadActive; });

        if (!guestThreadActive)
            break;

        auto message = std::move(guestMessageQueue.front());
        guestMessageQueue.pop();
        lock.unlock();

        if (message.action == "get_recommendations")
        {
            std::cout << "Guest thread " << thread_id << " - Generating recommendations for video " << message.videoId << std::endl;
        }

        message.threadIdPromise.set_value(thread_id);
    }

    std::cout << "Guest thread " << thread_id << " ended" << std::endl;
}
