package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.follow.FollowRequestDto;
import com.cocomoo.taily.dto.follow.FollowResponseDto;
import com.cocomoo.taily.dto.follow.FollowUserResponseDto;
import com.cocomoo.taily.entity.Follow;
import com.cocomoo.taily.entity.FollowState;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.FollowRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 팔로우
    @Transactional
    public FollowResponseDto follow(FollowRequestDto request) {
        if (request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }

        User follower = userRepository.findById(request.getFollowerId())
                .orElseThrow(() -> new IllegalArgumentException("팔로워 사용자 없음"));
        User following = userRepository.findById(request.getFollowingId())
                .orElseThrow(() -> new IllegalArgumentException("팔로잉 사용자 없음"));

        Optional<Follow> existing = followRepository.findByFollowerAndFollowing(follower, following);

        Follow follow;
        if (existing.isPresent()) {
            follow = existing.get();
            if (follow.getState() == FollowState.INACTIVE) {
                follow.activate(); // INACTIVE -> ACTIVE
                followRepository.save(follow);
            } else {
                throw new IllegalStateException("이미 팔로우한 사용자입니다."); // ACTIVE
            }
        } else {
            // 새로운 팔로우 생성
            follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .state(FollowState.ACTIVE)
                    .build();
            followRepository.save(follow);
        }

        return FollowResponseDto.from(follow);
    }



    // 언팔로우
    @Transactional
    public void unfollow(FollowRequestDto request) {
        User follower = userRepository.findById(request.getFollowerId())
                .orElseThrow(() -> new IllegalArgumentException("팔로워 사용자 없음"));
        User following = userRepository.findById(request.getFollowingId())
                .orElseThrow(() -> new IllegalArgumentException("팔로잉 사용자 없음"));

        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresent(f -> {
                    f.deactivate();
                    followRepository.save(f);
                    log.info("언팔로우 완료: {} -> {}", follower.getUsername(), following.getUsername());
                });
    }

    // 팔로잉 리스트
    public List<FollowUserResponseDto> getFollowingList(Long userId) {
        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        return followRepository.findByFollowerAndState(follower, FollowState.ACTIVE)
                .stream()
                .map(f -> FollowUserResponseDto.from(f.getFollowing()))
                .collect(Collectors.toList());
    }

    // 팔로워 리스트
    public List<FollowUserResponseDto> getFollowerList(Long userId) {
        User following = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        return followRepository.findByFollowingAndState(following, FollowState.ACTIVE)
                .stream()
                .map(f -> FollowUserResponseDto.from(f.getFollower()))
                .collect(Collectors.toList());
    }
}
