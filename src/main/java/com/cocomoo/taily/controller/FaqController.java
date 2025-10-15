package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.cs.FaqDetailResponseDto;
import com.cocomoo.taily.dto.cs.FaqPageResponseDto;
import com.cocomoo.taily.dto.cs.FaqRequestDto;
import com.cocomoo.taily.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
@Slf4j
public class FaqController {
    private final FaqService faqService;

    @GetMapping
    public ResponseEntity<?> getAllFaqs(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        log.info("faq 리스트 조회 시작");
        FaqPageResponseDto result = faqService.getFaqPage(page - 1, size);
        log.info("faq 리스트 조회 요청 {}", result.getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 faq 리스트 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFaqById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        FaqDetailResponseDto faq = faqService.getFaqById(id, username);

        return ResponseEntity.ok(ApiResponseDto.success(faq, "faq 상세 조회 성공"));
    }

    @PostMapping
    public ResponseEntity<?> createFaq(@RequestBody FaqRequestDto faqRequestDto) {
        log.info("Faq 작성 시작");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        FaqRequestDto requestDto = FaqRequestDto.builder()
                .title(faqRequestDto.getTitle())
                .content(faqRequestDto.getContent())
                .build();

        FaqDetailResponseDto faqDetailResponseDto = faqService.createFaq(requestDto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(faqDetailResponseDto, "faq가 작성되었습니다."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable Long id, @RequestBody FaqRequestDto faqRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        FaqDetailResponseDto updatedFaq = faqService.updateFaq(id, faqRequestDto, username);

        return ResponseEntity.ok(ApiResponseDto.success(updatedFaq, "faq 수정 성공"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFaq(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        faqService.deleteFaq(id, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "faq 삭제 성공"));
    }

}
