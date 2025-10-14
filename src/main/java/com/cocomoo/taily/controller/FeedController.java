package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.security.user.CustomUserDetails;
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

    // 새로운 피드 등록 메서드
    // Spring Seucurity에서 현재 로그인 유저의 id 가져오기
    @PostMapping
    public ResponseEntity<FeedResponseDto> createFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // @RequestBody FeedRequestDto dto, // JSON으로 받음
            @RequestPart("feed") FeedRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        dto.setImages(images); // images가 null일 수도 있음
        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(feedService.registerFeed(userId, dto));
    }

    // Feed id를 기준으로 feed 객체 반환
    @GetMapping("/{id}")
    public ResponseEntity<FeedResponseDto> getFeed(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.getFeed(id));
    }
    
    // 전체 피드 조회
    @GetMapping
    public ResponseEntity<List<FeedResponseDto>> getAllFeeds() {
        List<FeedResponseDto> feeds = feedService.getAllFeeds();
        return ResponseEntity.ok(feeds);
    }

    // 피드 삭제 메서드
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteFeed(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = Long.parseLong(user.getUsername());
        feedService.deleteFeed(id, userId);
        return ResponseEntity.noContent().build();
    }
}
