package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.User.UserUpdateRequestDto;
import com.cocomoo.taily.dto.inquiry.InquiryPageResponseDto;
import com.cocomoo.taily.dto.inquiry.InquiryResponseDto;
import com.cocomoo.taily.dto.myPage.*;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.security.jwt.TokenBlacklistService;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.MyPageService;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final UserService userService;
    private final MyPageService myPageService;
    private final TokenBlacklistService tokenBlacklistService;

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
        String publicId = userDetails.getUser().getPublicId();

        // 서비스에서 유저 정보 조회
        UserProfileResponseDto response = myPageService.getMyInfo(publicId);

        log.info("내 정보 조회 완료: username={}, nickname={}", response.getUsername(), response.getNickname());

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 검증 컨트롤러
     * @param userDetails
     * @param request
     * @return
     */
    @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request
    ) {
        String username = userDetails.getUsername();
        String inputPassword = request.get("password");

        boolean isValid = userService.validatePassword(username, inputPassword);

        if (!isValid) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "비밀번호가 일치하지 않습니다."));
        }

        return ResponseEntity.ok(Map.of("message", "비밀번호 검증 성공"));
    }

    /**
     * 사용자 정보 수정 (이미지 업로드는 ImageController로 분리됨)
     * - 텍스트 정보만 수정
     * - 프로필 이미지는 /api/images/upload (tableTypesId=1)로 별도 처리
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
        log.info("내 정보 수정 API 호출: username={}, publicId={}",
                user.getUsername(), user.getPublicId());

        // 서비스 호출 - publicId 기반
        UserProfileResponseDto response =
                userService.updateMyProfileByPublicId(user.getPublicId(), requestDto);

        log.info("내 정보 수정 완료: nickname={}, email={}",
                response.getNickname(), response.getEmail());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @RequestBody Map<String, String> request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        String username = userDetails.getUsername();
        String password = request.get("password");

        // 비밀번호 검증
        boolean isValid = userService.validatePassword(username, password);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "비밀번호가 일치하지 않습니다."));
        }

        // 회원 탈퇴 처리
        userService.deleteMyAccount(username);
        log.info("회원 탈퇴 완료: username={}", username);

        // JWT 추출 및 블랙리스트 등록
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            tokenBlacklistService.add(token);  // 블랙리스트에 등록
            log.info("JWT 블랙리스트 등록 완료: {}", token);
        }

        // SecurityContext 초기화 (즉시 로그아웃 효과)
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    /**
     * 내 반려동물 프로필 작성
     */
    @PostMapping(value = "/mypet", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createMyPetProfile (
            @RequestPart("myPetProfile") MyPetProfileCreateRequestDto myPetProfileCreateRequestDto
    ) {
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
        log.info("내 반려동물 리스트 조회 완료 {} 건", myPetProfiles.size());

        return ResponseEntity.ok(ApiResponseDto.success(myPetProfiles, "나의 반려동물 프로필 리스트 조회 성공"));
    }

    /**
     * 내 반려동물 프로필 수정
     */
    @PutMapping(value = "/mypet/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMyPetProfile(
            @PathVariable Long id,
            @RequestPart("myPetProfile") MyPetProfileUpdateRequestDto myPetProfileUpdateRequestDto
    ) {
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

    @GetMapping("/mywalk-paths")
    public ResponseEntity<?> getMyWalkPaths(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Page<MyWalkPathListResponseDto> postsPage = myPageService.getMyWalkPaths(
                username, page, size);

        return ResponseEntity.ok(ApiResponseDto.success(postsPage, "내가 작성한 게시글 조회 성공"));
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

    // 내 좋아요 리스트
    @GetMapping("/myLikes")
    public ResponseEntity<?> getMyLikes(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "5") int size) {
        log.info("내 좋아요 리스트 조회");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

//        List<MyLikesResponseDto> myLikes = myPageService.getMyLikes(username);
        MyLikesPageResponseDto myLikesPage = myPageService.getMyLikesPage(username, page - 1, size);
        log.info("내 좋아요 리스트 조회 완료 {} 건", myLikesPage .getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(myLikesPage, "내 좋아요 리스트 조회 성공"));

    }

    // 좋아요 토글 API
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleLike(
            @RequestParam Long postsId,
            @RequestParam Long tableTypeId
    ) {
        log.info("좋아요 토글 요청: postsId={}, tableTypeId={}", postsId, tableTypeId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 토글 처리
        boolean newState = myPageService.toggleLike(username, postsId, tableTypeId);
        log.info("좋아요 토글 완료: postsId={}, tableTypeId={}, 상태={}", postsId, tableTypeId, newState);

        return ResponseEntity.ok(ApiResponseDto.success(newState, "토글 상태 변화 성공"));
    }

    // 내 문의 조회
    @GetMapping("/inquiries")
    public ResponseEntity<?> getMyInquiries(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("내 문의 내역 조회 요청, username={}, keyword={}, page={}, size={}", username, keyword, page, size);

        InquiryPageResponseDto result = myPageService.getUserInquiriesPage(username, keyword, page - 1, size);

        log.info("내 문의 내역 조회 결과, totalCount={}", result.getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(result, "내 문의 내역 조회 성공"));
    }

    // 내 특정 문의 조회
    @GetMapping("/inquiries/{id}")
    public ResponseEntity<?> getInquiryWithReply(@PathVariable Long id) {
        InquiryResponseDto response = myPageService.getInquiryWithReply(id);
        log.info("문의 + 답변 조회, ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success(response, "문의 + 답변 조회 성공"));
    }

    // 내 피드

}