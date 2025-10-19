package com.cocomoo.taily.controller;

import com.cocomoo.taily.entity.Notice;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 관리자 - 공지 등록
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Notice> createNotice(
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal User user
    ) {
        Notice saved = noticeService.createNotice(title, content, user);
        return ResponseEntity.ok(saved);
    }

    /**
     * 사용자, 관리자 - 공지 전체 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Notice>> getAllNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    /**
     * 사용자, 관리자 - 공지 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    /**
     * 관리자 - 공지 수정
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Notice> updateNotice(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content
    ) {
        Notice updated = noticeService.updateNotice(id, title, content);
        return ResponseEntity.ok(updated);
    }

    /**
     * 관리자 - 공지 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
