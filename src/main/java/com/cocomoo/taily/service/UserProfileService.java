package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.userprofile.UserProfileSummaryResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileService {


    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FeedRepository feedRepository;
    private final TailyFriendRepository tailyFriendRepository;
    private final WalkPathRepository walkPathRepository;

    public UserProfileSummaryResponseDto getUserProfileSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Long followerCount = followRepository.countFollowers(userId);
        Long followingCount = followRepository.countFollowings(userId);

        Long feedCount = feedRepository.countFeedsByUserId(userId);
        Long tailyFriendCount = tailyFriendRepository.countTailyFriendsByUserId(userId);
        Long walkPathCount = walkPathRepository.countWalkPathsByUserId(userId);

        Long totalCount = feedCount + tailyFriendCount + walkPathCount;

        return UserProfileSummaryResponseDto.from(user, followerCount, followingCount, totalCount);
    }
}
