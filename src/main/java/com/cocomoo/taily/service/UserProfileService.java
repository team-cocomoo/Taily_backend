package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.myPage.UserProfileResponseDto;
import com.cocomoo.taily.dto.userprofile.OtherUserProfileResponseDto;
import com.cocomoo.taily.dto.userprofile.OtherUserProfileSummaryResponseDto;
import com.cocomoo.taily.entity.Feed;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.Pet;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileService {


    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FeedRepository feedRepository;
    private final TailyFriendRepository tailyFriendRepository;
    private final MyPetRepository myPetRepository;
    private final WalkPathRepository walkPathRepository;
    private final ImageRepository imageRepository;

    // 다른 회원 정보(요약)
    public OtherUserProfileSummaryResponseDto getUserProfileSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Long followerCount = followRepository.countFollowers(userId);
        Long followingCount = followRepository.countFollowings(userId);

        Long feedCount = feedRepository.countFeedsByUserId(userId);
        Long tailyFriendCount = tailyFriendRepository.countTailyFriendsByUserId(userId);
        Long walkPathCount = walkPathRepository.countWalkPathsByUserId(userId);

        Long postCount = feedCount + tailyFriendCount + walkPathCount;

        return OtherUserProfileSummaryResponseDto.from(user, followerCount, followingCount, postCount);
    }

    // 다른 회원 정보(상세)
    public OtherUserProfileResponseDto getOtherUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. id=" + userId));

        Long followerCount = followRepository.countFollowers(userId);
        Long followingCount = followRepository.countFollowings(userId);
        Long feedCount = feedRepository.countFeedsByUserId(userId);
        Long tailyFriendCount = tailyFriendRepository.countTailyFriendsByUserId(userId);
        Long walkPathCount = walkPathRepository.countWalkPathsByUserId(userId);

        Long postCount = feedCount + tailyFriendCount + walkPathCount;

        // 반려동물 목록
        List<Pet> pets = myPetRepository.findMyPetProfilesByPetOwner(user.getUsername());

        // 피드 목록
        List<Feed> feeds = feedRepository.findByUserId(userId);

        // 피드 이미지 목록
        List<Image> feedImages = imageRepository.findFeedImagesByUserId(userId);

        List<Image> petImages = imageRepository.findPetImagesByUserId(userId);

        // DTO 변환
        return OtherUserProfileResponseDto.from(
                user,
                followerCount,
                followingCount,
                postCount,
                pets,
                feeds,
                feedImages,
                petImages
        );
    }
}
