package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.myPage.UserProfileResponseDto;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserProfileResponseDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("내 정보 조회 API 호출: username={}", username);
        UserProfileResponseDto response = userService.getMyInfo(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 현재 로그인한 사용자 정보 수정
     */
/*    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        String username = userDetails.getUsername();
        log.info("내 정보 수정 API 호출: username={}", username);
        UserProfileResponseDto response = userService.updateMyProfile(username, requestDto);
        return ResponseEntity.ok(response);
    }*/


}