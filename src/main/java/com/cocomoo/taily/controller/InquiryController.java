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
}
