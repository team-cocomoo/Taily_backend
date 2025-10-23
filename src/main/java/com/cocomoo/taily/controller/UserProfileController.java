package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.userprofile.OtherUserProfileResponseDto;
import com.cocomoo.taily.dto.userprofile.OtherUserProfileSummaryResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.FeedRepository;
import com.cocomoo.taily.repository.FollowRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.service.UserProfileService;
import com.cocomoo.taily.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/{id}")
    public ResponseEntity<OtherUserProfileSummaryResponseDto> getUserProfileSummary(@PathVariable Long id) {
        OtherUserProfileSummaryResponseDto summary = userProfileService.getUserProfileSummary(id);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<OtherUserProfileResponseDto> getOtherUserProfile(@PathVariable Long id) {
        OtherUserProfileResponseDto profile = userProfileService.getOtherUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * 추가: publicId 기반 요약 정보 조회
     */
    @GetMapping("/public/{publicId}")
    public ResponseEntity<OtherUserProfileSummaryResponseDto> getUserProfileSummaryByPublicId(
            @PathVariable String publicId) {
        log.debug("publicId로 사용자 요약 조회 요청: {}", publicId);
        OtherUserProfileSummaryResponseDto summary = userProfileService.getUserProfileSummaryByPublicId(publicId);
        return ResponseEntity.ok(summary);
    }

    /**
     * 추가: publicId 기반 전체 프로필 조회
     */
    @GetMapping("/public/{publicId}/profile")
    public ResponseEntity<OtherUserProfileResponseDto> getOtherUserProfileByPublicId(
            @PathVariable String publicId) {
        log.debug("publicId로 사용자 상세 프로필 조회 요청: {}", publicId);
        OtherUserProfileResponseDto profile = userProfileService.getOtherUserProfileByPublicId(publicId);
        return ResponseEntity.ok(profile);
    }
}
