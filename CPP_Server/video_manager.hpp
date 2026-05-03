#ifndef VIDEO_MANAGER_HPP
#define VIDEO_MANAGER_HPP

#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <mutex>

struct VideoData
{
    std::unordered_set<std::string> viewers;
    int totalViews;
};

class VideoManager
{
public:
    static VideoManager &getInstance();
    void updateVideoData(const std::unordered_map<std::string, int> &newVideoData);
    std::vector<std::string> getRecommendedVideos(const std::string &currentVideoId, const std::string &userId, int numRecommendations);
    void addViewer(const std::string& videoId, const std::string& userId);

private:
    VideoManager() = default;
    std::unordered_map<std::string, VideoData> videos;
    std::mutex videoMutex;
};

#endif