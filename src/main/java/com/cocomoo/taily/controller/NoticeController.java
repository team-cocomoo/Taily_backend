package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.notice.NoticeRequestDto;
import com.cocomoo.taily.dto.notice.NoticeResponseDto;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지 등록 (관리자만)
     * JWT 토큰에서 publicId 추출 → 서비스로 전달
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<NoticeResponseDto> createNotice(
            @RequestBody NoticeRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String publicId = userDetails.getPublicId();
        log.info("[공지등록] 요청자 publicId={}", publicId);

        NoticeResponseDto saved = noticeService.createNotice(dto, publicId);
        return ResponseEntity.ok(saved);
    }

    /**
     * 공지 목록 조회 (검색 + 페이지네이션)
     */
    @GetMapping
    public ResponseEntity<Page<NoticeResponseDto>> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<NoticeResponseDto> notices = noticeService.getNotices(pageable, keyword);
        return ResponseEntity.ok(notices);
    }

    /**
     * 공지 상세 조회 (조회수 증가)
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long id) {
        NoticeResponseDto notice = noticeService.getNotice(id);
        return ResponseEntity.ok(notice);
    }

    /**
     * 공지 수정 (관리자)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String publicId = userDetails.getPublicId();
        log.info("[공지수정] 요청자 publicId={}, noticeId={}", publicId, id);

        NoticeResponseDto updated = noticeService.updateNotice(id, dto, publicId);
        return ResponseEntity.ok(updated);
    }

    /**
     * 공지 삭제 (관리자)
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String publicId = userDetails.getPublicId();
        log.info("[공지삭제] 요청자 publicId={}, noticeId={}", publicId, id);

        noticeService.deleteNotice(id, publicId);
        return ResponseEntity.noContent().build();
    }
}
