package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendCreateRequestDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendListResponseDto;
import com.cocomoo.taily.service.TailyFriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taily-friends")
@RequiredArgsConstructor
@Slf4j
public class TailyFriendController {
    private final TailyFriendService tailyFriendService;

    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<?> getTailyFriends(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size) {
        List<TailyFriendListResponseDto> posts = tailyFriendService.getTailyFriendsPage(page-1, size);
        return ResponseEntity.ok(ApiResponseDto.success(posts, "게시물 목록 조회 성공"));
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getTailyFriendById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        TailyFriendDetailResponseDto post = tailyFriendService.getTailyFriendById(id,username);
        log.info("게시글 조회 성공 : title = {}",post.getTitle());
        return ResponseEntity.ok(ApiResponseDto.success(post,"게시글 조회 성공"));
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<?> createTailyFriend(@RequestBody TailyFriendCreateRequestDto requestDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 작성 , 작성자 {}", username);

        TailyFriendDetailResponseDto tailyFriendDetailResponseDto = tailyFriendService.createTailyFriend(requestDto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(tailyFriendDetailResponseDto,"게시글 작성 성공."));
    }

    // 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTailyFriend(@PathVariable Long id,
                                               @RequestBody TailyFriendCreateRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 수정 , 작성자 {}", username);

        TailyFriendDetailResponseDto updatedPost = tailyFriendService.updateTailyFriend(id, username, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updatedPost, "게시글 수정 성공"));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTailyFriend(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 삭제 , 작성자 {}", username);

        tailyFriendService.deleteTailyFriend(id, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "게시글 삭제 성공"));
    }

    // 좋아요 상태 변경
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("좋아요 상태 변경 , 작성자 {}", username);

        tailyFriendService.toggleLike(id, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "좋아요 상태 변경"));
    }

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> createComment(@PathVariable Long id,
                                           @RequestBody CommentCreateRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("댓글 작성 , 작성자 {}", username);

        CommentResponseDto dto = tailyFriendService.createComment(id, username, requestDto);
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
        CommentResponseDto dto = tailyFriendService.createComment(id, username,
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

        List<CommentResponseDto> comments = tailyFriendService.getCommentsPage(id, page-1, size);
        return ResponseEntity.ok(ApiResponseDto.success(comments, "댓글 목록 조회 성공"));
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
        CommentResponseDto updatedComment = tailyFriendService.updateComment(commentId, username, newContent);
        return ResponseEntity.ok(ApiResponseDto.success(updatedComment, "댓글 수정 성공"));
    }

    // 댓글 삭제
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,
                                           @PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("댓글 삭제 , 작성자 {}", username);

        tailyFriendService.deleteComment(commentId, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "댓글 삭제 성공"));
    }

    // 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchTailyFriendsPage(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        List<TailyFriendListResponseDto> results = tailyFriendService.searchTailyFriendsPage(keyword, page, size);
        return ResponseEntity.ok(ApiResponseDto.success(results, "검색 결과"));
    }
}
