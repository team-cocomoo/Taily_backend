package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Feed;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedService {
    Feed createFeed(Long userId, String content, List<MultipartFile> images);
    Feed getFeed(Long feedId);
    List<Feed> getAllFeeds();
    Feed updateFeed(Long feedId, Long userId, String content, List<MultipartFile> images);
    void deleteFeed(Long feedId, Long userId);
}
