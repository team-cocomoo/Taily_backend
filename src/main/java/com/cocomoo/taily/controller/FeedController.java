package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FeedController
 * - 이미지 업로드는 /api/images/upload 에서 별도 수행
 * - 본 컨트롤러는 content, tags, imagePaths 기반의 JSON만 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * 1. 피드 등록
     * - JSON 구조: { "content": "...", "tags": ["#강아지", "#산책"], "imagePaths": ["/uploads/feed/xxx.jpg"] }
     * - 이미지 업로드는 /api/images/upload 호출 후 filePath를 전달
     */
    @PostMapping(consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedResponseDto> createFeed(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody FeedRequestDto dto
    ) {
        try {
            log.info("피드 등록 요청: userPublicId={}, content={}, tags={}",
                    user.getPublicId(), dto.getContent(), dto.getTags());

            FeedResponseDto created = feedService.registerFeed(user.getUserId(), dto);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("피드 등록 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 2. 무한 스크롤 기반 피드 목록 조회
     * - page, size 파라미터 (기본값 page=0, size=10)
     */
    @GetMapping
    public ResponseEntity<Page<FeedResponseDto>> getFeeds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("피드 목록 조회 요청: page={}, size={}", page, size);
        Page<FeedResponseDto> feeds = feedService.getFeedsWithPaging(page, size);
        return ResponseEntity.ok(feeds);
    }

    /**
     * 3. 피드 상세 조회
     * - 조회수 증가 포함
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedResponseDto> getFeed(@PathVariable Long id) {
        log.debug("피드 상세 조회 요청: id={}", id);
        FeedResponseDto feed = feedService.getFeed(id);
        return ResponseEntity.ok(feed);
    }

    /**
     * 4. 피드 수정용 데이터 조회
     * - 프론트 수정 페이지 진입 시 사용
     * - 기존 내용 + 태그 + 이미지 경로 반환
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedResponseDto> getFeedForEdit(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.debug("피드 수정 데이터 조회 요청: feedId={}, userPublicId={}", id, user.getPublicId());
        FeedResponseDto dto = feedService.getFeedForUpdate(id, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    /**
     * 5. 피드 수정
     * - JSON 구조: { "content": "...", "tags": [...], "imagePaths": [...] }
     * - 기존 이미지 삭제 후 새 경로 기반으로 저장
     */
    @PutMapping(value = "/{id}", consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedResponseDto> updateFeed(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody FeedRequestDto dto
    ) {
        try {
            log.info("피드 수정 요청: feedId={}, userPublicId={}, tags={}",
                    id, user.getPublicId(), dto.getTags());
            FeedResponseDto updated = feedService.updateFeed(id, user.getUserId(), dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("피드 수정 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 6. 피드 삭제
     * - 본인 게시물만 가능
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFeed(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        try {
            log.info("피드 삭제 요청: feedId={}, userPublicId={}", id, user.getPublicId());
            feedService.deleteFeed(id, user.getUserId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("피드 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/myfeed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FeedResponseDto>> getMyFeeds(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("내 피드 목록 조회 요청: userId={}, page={}, size={}", user.getUserId(), page, size);
        Page<FeedResponseDto> feeds = feedService.getMyFeeds(user.getUserId(), page, size);
        return ResponseEntity.ok(feeds);
    }

    /** -------------------- 댓글 CRUD -------------------- **/

// ✅ 댓글 작성
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> createComment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CommentCreateRequestDto requestDto
    ) {
        CommentResponseDto dto = feedService.createComment(id, user.getUserId(), requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(dto, "댓글 작성 성공"));
    }

    // ✅ 대댓글 작성
    @PostMapping("/{id}/comments/{parentId}/reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> createReply(
            @PathVariable Long id,
            @PathVariable Long parentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CommentCreateRequestDto requestDto
    ) {
        CommentCreateRequestDto dtoWithParent = CommentCreateRequestDto.builder()
                .content(requestDto.getContent())
                .parentCommentsId(parentId)
                .build();

        log.info("대댓글 요청 - feedId={}, parentId={}, content={}", id, parentId, requestDto.getContent());

        CommentResponseDto dto = feedService.createComment(id, user.getUserId(), dtoWithParent);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(dto, "대댓글 작성 성공"));
    }

    // 댓글 조회 (페이징)
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Map<String, Object> response = feedService.getCommentsPage(id, page - 1, size);
        return ResponseEntity
                .ok(ApiResponseDto.success(response, "댓글 목록 조회 성공"));
    }

    // ✅ 댓글 수정
    @PatchMapping("/{id}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, String> request
    ) {
        String newContent = request.get("content");
        CommentResponseDto updated =
                feedService.updateComment(commentId, user.getUserId(), newContent);
        return ResponseEntity
                .ok(ApiResponseDto.success(updated, "댓글 수정 성공"));
    }

    // ✅ 댓글 삭제
    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        feedService.deleteComment(commentId, user.getUserId());
        return ResponseEntity
                .ok(ApiResponseDto.success(null, "댓글 삭제 성공"));
    }

}