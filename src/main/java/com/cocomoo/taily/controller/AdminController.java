package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.admin.AdminLoginRequestDto;
import com.cocomoo.taily.dto.admin.AdminLoginResponseDto;
import com.cocomoo.taily.dto.admin.AdminUserResponseDto;
import com.cocomoo.taily.dto.admin.UserPageResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.security.jwt.JwtUtil;
import com.cocomoo.taily.service.AdminService;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    public final AdminService adminService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 로그인 : JsonLoginFilter에서 구현


    // 전체 회원 출력
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("전체 회원 리스트 조회 요청, keyword={}, page={}, size={}", keyword, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);

        UserPageResponseDto result = adminService.getUsersPage(keyword, page - 1, size);
        log.info("전체 회원 리스트 조회 요청 {}", result.getTotalCount());


        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 회원 리스트 조회 성공"));
    }

    // 회원 정보 출력
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserInfoById(@PathVariable Long id) {
        AdminUserResponseDto userInfo = userService.findUserInfoById(id);

        return ResponseEntity.ok(ApiResponseDto.success(userInfo, "회원 정보 조회 성공"));
    }

}
