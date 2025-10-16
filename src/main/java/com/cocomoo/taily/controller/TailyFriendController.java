package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.tailyFriends.*;
import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.service.AlarmService;
import com.cocomoo.taily.service.TailyFriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taily-friends")
@RequiredArgsConstructor
@Slf4j
public class TailyFriendController {
    private final TailyFriendService tailyFriendService;
    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<?> getTailyFriends(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size) {

        TailyFriendPageResponseDto response = tailyFriendService.getTailyFriendsPage(page - 1, size);
        return ResponseEntity.ok(ApiResponseDto.success(response, "게시물 목록 조회 성공"));
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

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long id,
            @RequestBody CommentCreateRequestDto requestDto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("댓글 작성 , 작성자 {}", username);

        CommentResponseDto dto = tailyFriendService.createComment(id, username, requestDto);

        // 댓글 작성 시 알람 위임
        try {
            alarmService.sendCommentAlarm(username, id, null);
            log.info("댓글 알람 전송 시도 → 게시글 ID: {}", id);
        } catch (Exception e) {
            log.error("댓글 알람 전송 실패 → 게시글 ID: {}, 작성자: {}", id, username, e);
        }

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

        // 대댓글 작성 시 알람 전송 (부모 댓글 작성자가 자기 자신이면 알람 생략)
        try {
            Comment parentComment = tailyFriendService.getCommentById(parentId);
            if (!parentComment.getUsersId().getUsername().equals(username)) {
                alarmService.sendCommentAlarm(username, id, parentId);
                log.info("대댓글 알람 전송 시도 → 게시글 ID: {}, 부모 댓글 ID: {}", id, parentId);
            } else {
                log.info("자기 자신 댓글에 답글 작성 - 알람 전송 생략");
            }
        } catch (Exception e) {
            log.error("대댓글 알람 전송 실패 → 게시글 ID: {}, 부모 댓글 ID: {}, 작성자: {}", id, parentId, username, e);
        }

        return ResponseEntity.ok(ApiResponseDto.success(dto, "대댓글 작성 성공"));
    }

    // 댓글 조회
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getCommentsPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        // page - 1 → 0 기반으로 Service에 전달
        Map<String, Object> response = tailyFriendService.getCommentsPage(id, page - 1, size);
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

    // 주소만 검색
    @GetMapping("/addresses")
    public ResponseEntity<?> getAllAddresses() {
        List<TailyFriendAddressResponseDto> addresses = tailyFriendService.getAllAddresses();
        log.info("전체 게시글 주소 조회, 개수: {}", addresses.size());
        return ResponseEntity.ok(ApiResponseDto.success(addresses, "전체 주소 조회 성공"));
    }
}
