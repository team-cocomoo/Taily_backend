package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.User.UserUpdateRequestDto;
import com.cocomoo.taily.dto.follow.FollowUserResponseDto;
import com.cocomoo.taily.dto.myPage.*;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.MyPageService;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final UserService userService;
    private final MyPageService myPageService;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        // 1. CustomUserDetails에서 User 객체 가져오기
        User user = userDetails.getUser();
        // 2. 로그 출력
        log.info("내 정보 조회 API 호출: username={}, publicId={}", user.getUsername(), user.getPublicId());
        // 3. DTO 변환 (publicId 기준)
        UserProfileResponseDto response = UserProfileResponseDto.from(user);

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        if (userDetails == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        User user = userDetails.getUser();
        log.info("내 정보 수정 API 호출: username={}, publicId={}", user.getUsername(), user.getPublicId());

        // 서비스 호출: publicId 기반으로 사용자 조회 후 수정
        UserProfileResponseDto response = userService.updateMyProfileByPublicId(user.getPublicId(), requestDto);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 반려동물 프로필 작성
     */
    @PostMapping("/mypet")
    public ResponseEntity<?> createMyPetProfile (@RequestBody MyPetProfileCreateRequestDto myPetProfileCreateRequestDto) {
        log.info("내 반려동물 프로필 작성 시작!");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        log.info("내 반려동물 프로필 작성, 주인: username={}",username);

        MyPetProfileResponseDto mypetProfileResponseDto = myPageService.createMyPetProfile(myPetProfileCreateRequestDto, username);

        log.info("내 반려동물 프로필 {}", mypetProfileResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(mypetProfileResponseDto, "내 반려동물 프로필이 등록되었습니다."));
    }

    /**
     * 내 반려동물 리스트 조회
     */
    @GetMapping("/mypet")
    public ResponseEntity<?> getAllMyPetProfile() {
        log.info("내 반려동물 리스트 조회");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        List<MyPetProfileResponseDto> myPetProfiles = myPageService.getMyPetProfiles(username);
        log.info("산책 일지 리스트 조회 완료 {} 건", myPetProfiles.size());

        return ResponseEntity.ok(ApiResponseDto.success(myPetProfiles, "나의 반려동물 프로필 리스트 조회 성공"));
    }

    /**
     * 내 반려동물 프로필 수정
     */
    @PutMapping("/mypet/{id}")
    public ResponseEntity<?> updateMyPetProfile(@PathVariable Long id, @RequestBody MyPetProfileUpdateRequestDto myPetProfileUpdateRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        MyPetProfileResponseDto updatedMyPetProfile = myPageService.updateMyPetProfile(id, myPetProfileUpdateRequestDto, username);

        return ResponseEntity.ok(ApiResponseDto.success(updatedMyPetProfile, "내 반려동물 프로필 수정 성공"));
    }

    /**
     * 내 반려동물 프로필 삭제
     */
    @DeleteMapping("/mypet/{id}")
    public ResponseEntity<?> deleteMyPetProfile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        myPageService.deleteMyPetProfile(id, username);

        return ResponseEntity.ok(ApiResponseDto.success(null, "내 반려동물 프로필 삭제 성공"));
    }

    @GetMapping("/mytaily-friends")
    public ResponseEntity<?> getMyTailyFriends(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Page<MyTailyFriendListResponseDto> postsPage = myPageService.getMyTailyFriends(
                username, page, size);

        return ResponseEntity.ok(ApiResponseDto.success(postsPage, "내가 작성한 게시글 조회 성공"));
    }

    @GetMapping("/following")
    public ResponseEntity<?> getFollowingUsers(
            @RequestParam(required = false) String nickname
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<MyFollowUserResponseDto> followingList;

        if (nickname != null && !nickname.isEmpty()) {
            followingList = myPageService.searchFollowingUsers(username, nickname);
        } else {
            followingList = myPageService.getFollowingUsers(username);
        }

        return ResponseEntity.ok(ApiResponseDto.success(followingList, "내가 팔로잉하는 유저 목록 조회 성공"));
    }

    @GetMapping("/followers")
    public ResponseEntity<?> getFollowerUsers(
            @RequestParam(required = false) String nickname
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<MyFollowUserResponseDto> followerList;

        if (nickname != null && !nickname.isEmpty()) {
            followerList = myPageService.searchFollowerUsers(username, nickname);
        } else {
            followerList = myPageService.getFollowerUsers(username);
        }

        return ResponseEntity.ok(ApiResponseDto.success(followerList, "나를 팔로우한 유저 목록 조회 성공"));
    }
}