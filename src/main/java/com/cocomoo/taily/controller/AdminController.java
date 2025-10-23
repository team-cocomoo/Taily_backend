package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.admin.AdminUserResponseDto;
import com.cocomoo.taily.dto.admin.UserPageResponseDto;
import com.cocomoo.taily.dto.admin.UserPenaltyResponseDto;
import com.cocomoo.taily.dto.common.report.ReportResponseDto;
import com.cocomoo.taily.dto.inquiry.InquiryPageResponseDto;
import com.cocomoo.taily.dto.inquiry.InquiryResponseDto;
import com.cocomoo.taily.security.jwt.JwtUtil;
import com.cocomoo.taily.service.AdminService;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
//        Pageable pageable = PageRequest.of(page - 1, size);

        UserPageResponseDto result = adminService.getUsersPage(keyword, page - 1, size);
        log.info("전체 회원 리스트 조회 요청 {}", result.getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 회원 리스트 조회 성공"));
    }

    // 회원 정보 출력
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserInfoById(@PathVariable Long id) {
        log.info("넘어온 파라미터 값: {}", id);
        AdminUserResponseDto userInfo = userService.findUserInfoById(id);

        log.info("조회해 온 userInfo: {}, imagePath: {}", userInfo.getUsername(), userInfo.getImagePath());
        return ResponseEntity.ok(ApiResponseDto.success(userInfo, "회원 정보 조회 성공"));
    }

    // 모든 신고 조회
    @GetMapping("/reports")
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReportResponseDto> reportsPage = adminService.getReportsPage(keyword, page - 1, size);

        long totalCount = reportsPage.getTotalElements();

        List<ReportResponseDto> content = reportsPage.getContent();

        return ResponseEntity.ok(ApiResponseDto.success(
                Map.of(
                        "reports", reportsPage.getContent(),
                        "totalCount", reportsPage.getTotalElements()
                ),
                "신고 리스트 조회 성공"
        ));
    }


    // 특정 신고 조회
    @GetMapping("/reports/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        ReportResponseDto report = adminService.getReportById(id);
        return ResponseEntity.ok(ApiResponseDto.success(report, "특정 신고 조회 성공"));
    }

    // 특정 유저 제재
    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspendUser(
            @PathVariable("id") Long userId,
            @RequestParam("days") int days
    ) {
        UserPenaltyResponseDto dto = adminService.suspendUser(userId, days);
        return ResponseEntity.ok(ApiResponseDto.success(dto, "특정 유저 제재 성공"));
    }

    // 신고에서 제재
    @PostMapping("/reports/{reportId}/suspend")
    public ResponseEntity<?> suspendReportedUser(
            @PathVariable("reportId") Long reportId,
            @RequestParam("days") int days
    ) {
        UserPenaltyResponseDto dto = adminService.suspendUserByReport(reportId, days);
        return ResponseEntity.ok(ApiResponseDto.success(dto, "신고에서 특정 유저 제재 성공"));
    }

    // 전체 문의 조회
    @GetMapping("/inquiries")
    public ResponseEntity<?> getAllInquiries(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("전체 문의 조회 요청, keyword={}, page={}, size={}", keyword, page, size);

        InquiryPageResponseDto result = adminService.getInquiriesPage(keyword, page - 1, size);

        log.info("전체 문의 조회 결과, totalCount={}", result.getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 문의 조회 성공"));
    }

    // 특정 문의 조회
    @GetMapping("/inquiries/{id}")
    public ResponseEntity<?> getInquiry(@PathVariable Long id) {
        InquiryResponseDto response = adminService.getInquiry(id);
        log.info("문의 조회, ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success(response, "문의 조회 성공"));
    }

    // 특정 문의 답변 조회 (답변 1개)
    @GetMapping("/inquiries/{id}/reply")
    public ResponseEntity<?> getReply(@PathVariable Long id) {
        InquiryResponseDto response = adminService.getReply(id);
        log.info("문의 답변 조회, 문의 ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success(response, "문의 답변 조회 성공"));
    }

    // 문의 삭제
    @DeleteMapping("/inquiries/{id}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("문의 삭제 요청, ID: {}, 요청자: {}", id, username);

        adminService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponseDto.success(null, "문의 삭제 성공"));
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAdminAccess() {
        return ResponseEntity.ok("관리자 권한 접근 성공");
    }

}
