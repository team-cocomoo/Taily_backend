package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.admin.UserPageResponseDto;
import com.cocomoo.taily.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    public final AdminService adminService;

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

        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 회원 리스트 조회 성공"));
    }
}
