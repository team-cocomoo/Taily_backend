package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.User.UserCreateRequestDto;
import com.cocomoo.taily.dto.User.UserLoginRequestDto;
import com.cocomoo.taily.dto.User.UserLoginResponseDto;
import com.cocomoo.taily.dto.User.UserResponseDto;
import com.cocomoo.taily.dto.myPage.UserProfileResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserState;
import com.cocomoo.taily.security.TokenBlacklistService;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * UserController - 회원 관련 API
 * 1. 회원가입
 * 2. 회원 조회
 * 3. 회원 정보 수정
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    // 로그아웃된 토큰 블랙리스트 처리하는 서비스

    /**
     * 1. 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> register(@RequestBody UserCreateRequestDto requestDto) {
        log.info("=== 회원가입 요청: username={}", requestDto.getUsername());

        // 예외는 GlobalExceptionHandler가 처리
        UserResponseDto responseDto = userService.register(requestDto);

        log.info("회원가입 성공: username={}", responseDto.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(responseDto, "회원가입이 완료되었습니다."));
    }

    /**
     * 2. username으로 회원 조회
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponseDto> getUserByUsername(@PathVariable String username) {
        log.info("회원 조회 API 호출: username={}", username);
        UserProfileResponseDto response = userService.findByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 현재 로그인한 사용자 정보 조회
     * (JWT 인증 후 SecurityContext에서 username 추출해서 전달)
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyInfo(Authentication authentication) {
        // Spring Security가 JWT에서 추출한 username
        String username = authentication.getName();

        log.info("내 정보 조회 API 호출: username={}", username);
        UserProfileResponseDto response = userService.getMyInfo(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 회원 정보 수정
     */
    @PutMapping("/{username}")
    public ResponseEntity<UserResponseDto> updateMember(
            @PathVariable String username,
            @RequestParam(required = false) String newUsername,
            @RequestParam(required = false) String newNickname,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String newTel,
            @RequestParam(required = false) String newEmail,
            @RequestParam(required = false) String newAddress,
            @RequestParam(required = false) String newIntroduction,
            @RequestParam(required = false) UserState newState
    ) {
        log.info("회원 정보 수정 API 호출: username={}", username);
        UserResponseDto response = userService.updateMember(
                username, newUsername, newNickname, newPassword, newTel, newEmail, newAddress, newIntroduction, newState
        );
        return ResponseEntity.ok(response);
    }

    // 로그인은 SecurityConfig에서 LoginFilter를 사용해서 구현되어 있음
    // 여기에 로그인 메서드 구현하지 않는다.

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authoriztion") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.add(token); // 블랙리스트 등록
        }
        return ResponseEntity.ok("로그아웃 완료");
    }

}