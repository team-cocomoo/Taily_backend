package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @PostMapping
    public ResponseEntity<FeedResponseDto> createFeed(
            @AuthenticationPrincipal UserDetails user,
            @RequestPart("feed") FeedRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        dto.setImages(images);
        Long userId = Long.parseLong(user.getUsername());
        return ResponseEntity.ok(feedService.createFeed(userId, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedResponseDto> getFeed(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.getFeed(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = Long.parseLong(user.getUsername());
        feedService.deleteFeed(id, userId);
        return ResponseEntity.noContent().build();
    }
}
