package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taily-friends")
@RequiredArgsConstructor
@Slf4j
public class TailyFriendController {
    private final TailyFriendService tailyFriendService;

    // 테일리 프렌즈 게시글 전체 조회
    @GetMapping
    public ResponseEntity<?> getAllTailyFriends(){
        List<TailyFriendListResponseDto> posts = tailyFriendService.getAllTailyFriends();
        return ResponseEntity.ok(ApiResponseDto.success(posts,"게시물 목록 조회 성공"));
    }

    // 테일리 프렌즈 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getTailyFriendById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        TailyFriendDetailResponseDto post = tailyFriendService.getTailyFriendById(id,username);
        log.info("게시글 조회 성공 : title = {}",post.getTitle());
        return ResponseEntity.ok(ApiResponseDto.success(post,"게시글 조회 성공"));
    }

    // 테일리 프렌즈 게시글 작성하기
    @PostMapping
    public ResponseEntity<?> createTailyFriend(@RequestBody TailyFriendCreateRequestDto requestDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("게시글 작성 , 작성자 {}", username);

        TailyFriendDetailResponseDto tailyFriendDetailResponseDto = tailyFriendService.createTailyFriend(requestDto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(tailyFriendDetailResponseDto,"게시글이 작성되었습니다."));
    }

    // 좋아요 상태 변경
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        tailyFriendService.toggleLike(id, username);
        return ResponseEntity.ok(ApiResponseDto.success(null, "좋아요 상태가 변경되었습니다."));
    }
}
