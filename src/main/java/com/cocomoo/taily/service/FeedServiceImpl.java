package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.FeedRepository;
import com.cocomoo.taily.service.FeedService;
import com.cocomoo.taily.service.ImageService;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Override
    @Transactional
    public Feed createFeed(Long userId, String content, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        Feed feed = Feed.builder()
                .content(content)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        feedRepository.save(feed);

        if (images != null && !images.isEmpty()) {
            imageService.uploadImages(userId, 3L, feed.getId(), images);
        }

        return feed;
    }

    @Override
    @Transactional(readOnly = true)
    public Feed getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드 없음: " + feedId));
        feed.setView(feed.getView() + 1); // 조회수 증가
        feedRepository.save(feed);
        return feed;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feed> getAllFeeds() {
        return feedRepository.findAll();
    }

    @Override
    @Transactional
    public Feed updateFeed(Long feedId, Long userId, String content, List<MultipartFile> images) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드 없음: " + feedId));

        if (!feed.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한 없음");
        }

        feed.setContent(content);
        feed.setUpdatedAt(LocalDateTime.now());
        feedRepository.save(feed);

        if (images != null && !images.isEmpty()) {
            imageService.uploadImages(userId, 3L, feed.getId(), images);
        }

        return feed;
    }

    @Override
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드 없음: " + feedId));

        if (!feed.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한 없음");
        }

        // 이미지 삭제
        List<Image> images = imageService.getImagesByTableAndPost(3L, feedId);
        for (Image img : images) {
            imageService.deleteImage(img.getId());
        }

        feedRepository.delete(feed);
    }
}
