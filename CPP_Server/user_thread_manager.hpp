#ifndef USER_THREAD_MANAGER_HPP
#define USER_THREAD_MANAGER_HPP

#include <string>
#include <unordered_map>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <queue>
#include <atomic>
#include <future>

struct UserMessage
{
    std::string action;
    std::string videoId; // Only used for notify-watch
    std::promise<std::string> threadIdPromise;
};

class UserThreadManager
{
public:
    static UserThreadManager &getInstance();
    ~UserThreadManager();
    std::string createThreadForUser(const std::string &userId);
    std::string closeThreadForUser(const std::string &userId);
    bool hasThreadForUser(const std::string &userId);
    std::future<std::string> processUserRequest(const std::string &userId, const std::string &action, const std::string &videoId = "");
    std::string getThreadIdForUser(const std::string &userId);
    std::future<std::string> processGuestRequest(const std::string &action, const std::string &videoId = "");

private:
    UserThreadManager(); // Declare the constructor as private
    UserThreadManager(const UserThreadManager &) = delete;
    UserThreadManager &operator=(const UserThreadManager &) = delete;
    std::unordered_map<std::string, std::thread> userThreads;
    std::unordered_map<std::string, std::queue<UserMessage>> userMessageQueues;
    std::unordered_map<std::string, std::mutex> userMutexes;
    std::unordered_map<std::string, std::condition_variable> userCondVars;
    std::unordered_map<std::string, std::atomic<bool>> userThreadActive;
    std::unordered_map<std::string, std::string> userThreadIds;
    std::mutex threadMapMutex;

    std::thread guestThread;
    std::queue<UserMessage> guestMessageQueue;
    std::mutex guestMutex;
    std::condition_variable guestCondVar;
    std::atomic<bool> guestThreadActive{true};

    void handleUserRequests(const std::string &userId);
    void handleGuestRequests();
};

#endif