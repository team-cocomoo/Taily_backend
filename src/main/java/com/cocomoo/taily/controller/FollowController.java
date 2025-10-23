package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.follow.FollowRequestDto;
import com.cocomoo.taily.dto.follow.FollowResponseDto;
import com.cocomoo.taily.dto.follow.FollowUserResponseDto;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.AlarmService;
import com.cocomoo.taily.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final FollowService followService;
    private final AlarmService alarmService;

    // 팔로우
    @PostMapping("/{followingId}")
    public ResponseEntity<ApiResponseDto> follow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long followingId) {

        FollowRequestDto request = new FollowRequestDto(userDetails.getUserId(), followingId);
        FollowResponseDto response = followService.follow(request);

        // 팔로우 시 알람 위임
        alarmService.sendFollowAlarm(userDetails.getUsername(), followingId);

        return ResponseEntity.ok(ApiResponseDto.success(response, "팔로우 성공"));
    }

    // 언팔로우
    @PatchMapping("/{followingId}/deactivate")
    public ResponseEntity<ApiResponseDto> unfollow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long followingId) {

        FollowRequestDto request = new FollowRequestDto(userDetails.getUserId(), followingId);
        followService.unfollow(request);

        return ResponseEntity.ok(ApiResponseDto.success(null, "언팔로우 성공"));
    }

    // 팔로잉 리스트
    @GetMapping("/following")
    public ResponseEntity<ApiResponseDto> getFollowingList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FollowUserResponseDto> list = followService.getFollowingList(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success(list, "팔로잉 리스트 조회 성공"));
    }

    // 팔로워 리스트
    @GetMapping("/followers")
    public ResponseEntity<ApiResponseDto> getFollowerList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FollowUserResponseDto> list = followService.getFollowerList(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success(list, "팔로워 리스트 조회 성공"));
    }

}
