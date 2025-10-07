package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.User.UserUpdateRequestDto;
import com.cocomoo.taily.dto.myPage.UserProfileResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final UserService userService;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 1. CustomUserDetails에서 User 객체 가져오기
        User user = userDetails.getUser();
        // 2. 로그 출력
        log.info("내 정보 조회 API 호출: username={}, publicId={}", user.getUsername(), user.getPublicId());
        // 3. DTO 변환 (publicId 기준)
        UserProfileResponseDto response = UserProfileResponseDto.from(user);

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        if (userDetails == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        User user = userDetails.getUser();
        log.info("내 정보 수정 API 호출: username={}, publicId={}", user.getUsername(), user.getPublicId());

        // 서비스 호출: publicId 기반으로 사용자 조회 후 수정
        UserProfileResponseDto response = userService.updateMyProfileByPublicId(user.getPublicId(), requestDto);

        return ResponseEntity.ok(response);
    }
}