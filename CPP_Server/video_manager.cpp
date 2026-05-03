#include "video_manager.hpp"
#include <algorithm>
#include <iostream>

VideoManager &VideoManager::getInstance()
{
    static VideoManager instance;
    return instance;
}

void VideoManager::updateVideoData(const std::unordered_map<std::string, int> &newVideoData)
{
    std::lock_guard<std::mutex> lock(videoMutex);

    // Remove videos that are no longer in the list
    for (auto it = videos.begin(); it != videos.end();)
    {
        if (newVideoData.find(it->first) == newVideoData.end())
        {
            it = videos.erase(it);
        }
        else
        {
            ++it;
        }
    }

    // Update existing videos and add new ones
    for (const auto &[videoId, views] : newVideoData)
    {
        if (videos.find(videoId) == videos.end())
        {
            videos[videoId] = VideoData{{}, views};
        }
        else
        {
            videos[videoId].totalViews = views;
        }
    }
}

std::vector<std::string> VideoManager::getRecommendedVideos(const std::string &currentVideoId, const std::string &userId, int numRecommendations)
{
    std::lock_guard<std::mutex> lock(videoMutex);

    std::vector<std::pair<std::string, int>> commonViewers;
    const auto &currentViewers = videos[currentVideoId].viewers;

    for (const auto &[videoId, videoData] : videos)
    {
        if (videoId != currentVideoId)
        {
            int commonCount = 0;
            for (const auto &viewer : videoData.viewers)
            {
                if (currentViewers.find(viewer) != currentViewers.end())
                {
                    commonCount++;
                }
            }
            commonViewers.emplace_back(videoId, commonCount);
        }
    }

    std::sort(commonViewers.begin(), commonViewers.end(),
              [this](const std::pair<std::string, int> &a, const std::pair<std::string, int> &b)
              {
                  if (a.second != b.second)
                      return a.second > b.second;
                  return videos.at(a.first).totalViews > videos.at(b.first).totalViews;
              });

    std::vector<std::string> recommendations;
    for (const auto &[videoId, _] : commonViewers)
    {
        if (recommendations.size() >= numRecommendations)
            break;
        recommendations.push_back(videoId);
    }

    // If we don't have enough recommendations, fill with most viewed videos
    if (recommendations.size() < numRecommendations)
    {
        std::vector<std::pair<std::string, int>> allVideos;
        for (const auto &[videoId, videoData] : videos)
        {
            if (videoId != currentVideoId && std::find(recommendations.begin(), recommendations.end(), videoId) == recommendations.end())
            {
                allVideos.emplace_back(videoId, videoData.totalViews);
            }
        }

        std::sort(allVideos.begin(), allVideos.end(),
                  [](const std::pair<std::string, int> &a, const std::pair<std::string, int> &b)
                  { return a.second > b.second; });

        for (const auto &[videoId, _] : allVideos)
        {
            if (recommendations.size() >= numRecommendations)
                break;
            recommendations.push_back(videoId);
        }
    }

    return recommendations;
}

void VideoManager::addViewer(const std::string &videoId, const std::string &userId)
{
    std::lock_guard<std::mutex> lock(videoMutex);
    videos[videoId].viewers.insert(userId);
}