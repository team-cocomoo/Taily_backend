package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.inquiry.InquiryCreateRequestDto;
import com.cocomoo.taily.dto.inquiry.InquiryPageResponseDto;
import com.cocomoo.taily.dto.inquiry.InquiryResponseDto;
import com.cocomoo.taily.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Slf4j
public class InquiryController {
    private final InquiryService inquiryService;

    // 문의 / 답변 작성
    @PostMapping
    public ResponseEntity<?> createInquiry(@RequestBody InquiryCreateRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("문의 작성 요청, 작성자: {}, 제목: {}", username, dto.getTitle());

        InquiryResponseDto response = inquiryService.createInquiry(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(response, "문의 작성 성공"));
    }

    // 전체 문의 조회
    @GetMapping
    public ResponseEntity<?> getAllInquiries(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("전체 문의 조회 요청, keyword={}, page={}, size={}", keyword, page, size);

        InquiryPageResponseDto result = inquiryService.getInquiriesPage(keyword, page - 1, size);

        log.info("전체 문의 조회 결과, totalCount={}", result.getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 문의 조회 성공"));
    }

    // 특정 문의 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getInquiry(@PathVariable Long id) {
        InquiryResponseDto response = inquiryService.getInquiry(id);
        log.info("문의 조회, ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success(response, "문의 조회 성공"));
    }

    // 특정 문의 답변 조회 (답변 1개)
    @GetMapping("/{id}/reply")
    public ResponseEntity<?> getReply(@PathVariable Long id) {
        InquiryResponseDto response = inquiryService.getReply(id);
        log.info("문의 답변 조회, 문의 ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success(response, "문의 답변 조회 성공"));
    }

    // 문의 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("문의 삭제 요청, ID: {}, 요청자: {}", id, username);

        inquiryService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponseDto.success(null, "문의 삭제 성공"));
    }
}
