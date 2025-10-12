package com.cocomoo.taily.controller;

import com.cocomoo.taily.entity.Feed;
import com.cocomoo.taily.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/petstory/feed")

@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @PostMapping
    public ResponseEntity<Feed> createFeed(
            @RequestParam Long userId,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        Feed feed = feedService.createFeed(userId, content, images);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<Feed> getFeed(@PathVariable Long feedId) {
        Feed feed = feedService.getFeed(feedId);
        return ResponseEntity.ok(feed);
    }

    @GetMapping
    public ResponseEntity<List<Feed>> getAllFeeds() {
        List<Feed> feeds = feedService.getAllFeeds();
        return ResponseEntity.ok(feeds);
    }

    @PutMapping("/{feedId}")
    public ResponseEntity<Feed> updateFeed(
            @PathVariable Long feedId,
            @RequestParam Long userId,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        Feed feed = feedService.updateFeed(feedId, userId, content, images);
        return ResponseEntity.ok(feed);
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(
            @PathVariable Long feedId,
            @RequestParam Long userId
    ) {
        feedService.deleteFeed(feedId, userId);
        return ResponseEntity.ok().build();
    }
}
