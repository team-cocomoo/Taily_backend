package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathCreateRequestDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathDetailResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathListResponseDto;
import com.cocomoo.taily.service.WalkPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/walk-paths")
@RequiredArgsConstructor
@Slf4j
public class WalkPathController {
    private final WalkPathService walkPathService;

    //walkPath 전체 게시물 조회
    @GetMapping
    public ResponseEntity<?> finaAllWalkPathList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size) {
        List<WalkPathListResponseDto> posts = walkPathService.findAllPostList(page-1,size);
        return ResponseEntity.ok(ApiResponseDto.success(posts, "게시물 목록 조회 성공"));
    }

    //walkpath 상세 게시물 조회

    @GetMapping("/{id}")
    public ResponseEntity<?> getWalkPathById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        WalkPathDetailResponseDto post = walkPathService.getWalkPathById(id,username);
        log.info("게시글 조회 성공 : title = {}",post.getTitle());
        return ResponseEntity.ok(ApiResponseDto.success(post,"게시글 조회 성공"));
    }

    //walkpath 게시글 작성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createWalkPath(
            @RequestBody WalkPathCreateRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 작성, 작성자 {}",username);

        WalkPathDetailResponseDto walkPathDetailResponseDto = walkPathService.createWalkPath(requestDto,username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(walkPathDetailResponseDto,"게시글 작성 성공."));
    }

    // walkpath 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatewalkPath(@PathVariable Long id,
                                               @RequestBody WalkPathCreateRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 수정 , 작성자 {}", username);

        WalkPathDetailResponseDto updatedPost = walkPathService.updateWalkPath(id, username, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updatedPost, "게시글 수정 성공"));
    }
    // walkpath 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWalkPath(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 삭제 , 작성자 {}", username);

        walkPathService.deleteWalkPath(id, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "게시글 삭제 성공"));
    }

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> createComment(@PathVariable Long id,
                                           @RequestBody CommentCreateRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("댓글 작성 , 작성자 {}", username);

        CommentResponseDto dto = walkPathService.createComment(id, username, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(dto, "댓글 작성 성공"));
    }

    // 대댓글 작성
    @PostMapping("/{id}/comments/{parentId}/reply")
    public ResponseEntity<?> createReply(@PathVariable Long id,
                                         @PathVariable Long parentId,
                                         @RequestBody CommentCreateRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("대댓글 작성 , 작성자 {}", username);

        // 서비스 호출 시 parentId 전달
        CommentResponseDto dto = walkPathService.createComment(id, username,
                CommentCreateRequestDto.builder()
                        .content(requestDto.getContent())
                        .parentCommentsId(parentId)
                        .build()
        );

        return ResponseEntity.ok(ApiResponseDto.success(dto, "대댓글 작성 성공"));
    }

    // 댓글 조회
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getCommentsPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        // page - 1 → 0 기반으로 Service에 전달
        Map<String, Object> response = walkPathService.getCommentsPage(id, page - 1, size);
        return ResponseEntity.ok(ApiResponseDto.success(response, "댓글 목록 조회 성공"));
    }

    // 댓글 수정
    @PatchMapping("/{id}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long id,
                                           @PathVariable Long commentId,
                                           @RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("댓글 수정 , 작성자 {}", username);

        String newContent = request.get("content");
        CommentResponseDto updatedComment = walkPathService.updateComment(commentId, username, newContent);
        return ResponseEntity.ok(ApiResponseDto.success(updatedComment, "댓글 수정 성공"));
    }

    // 댓글 삭제
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,
                                           @PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("댓글 삭제 , 작성자 {}", username);

        walkPathService.deleteComment(commentId, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "댓글 삭제 성공"));
    }

    // 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchWalkPathsPage(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        List<WalkPathListResponseDto> results = walkPathService.searchWalkPathsPage(keyword, page, size);
        return ResponseEntity.ok(ApiResponseDto.success(results, "검색 결과"));
    }


}
