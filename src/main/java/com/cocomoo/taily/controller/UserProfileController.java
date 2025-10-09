package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.userprofile.UserProfileSummaryResponseDto;
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
    public ResponseEntity<UserProfileSummaryResponseDto> getUserProfileSummary(@PathVariable Long id) {
        UserProfileSummaryResponseDto summary = userProfileService.getUserProfileSummary(id);
        return ResponseEntity.ok(summary);
    }
}
