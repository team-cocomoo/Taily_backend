package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Slf4j
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{tableTypeId}/{postId}")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long tableTypeId,
            @PathVariable Long postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean liked = likeService.toggleLike(postId, username, tableTypeId);
        return ResponseEntity.ok(ApiResponseDto.success(Map.of("liked", liked), "좋아요 상태 변경"));
    }

    @GetMapping("/{tableTypeId}/{postId}")
    public ResponseEntity<?> checkLike(
            @PathVariable Long tableTypeId,
            @PathVariable Long postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean liked = likeService.isLiked(postId, username, tableTypeId);
        return ResponseEntity.ok(ApiResponseDto.success(Map.of("liked", liked), "좋아요 상태 조회"));
    }
}
