package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.myPage.*;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MyPageService {
    public final MyPetRepository myPetRepository;
    public final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final TailyFriendRepository tailyFriendRepository;
    private final WalkPathRepository walkPathRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final MyLikesRepository myLikesRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public MyPetProfileResponseDto createMyPetProfile(MyPetProfileCreateRequestDto myPetProfileCreateRequestDto, String username) {
        log.info("=== 내 반려동물 프로필 작성 시작 : 주인={} ===", username);

        // 작성자 조회
        User petOwner = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        TableType tableType = tableTypeRepository.findById(2L).orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        // pet entity 생성
        Pet pet = Pet.builder()
                .name(myPetProfileCreateRequestDto.getName())
                .gender(myPetProfileCreateRequestDto.getGender())
                .preference(myPetProfileCreateRequestDto.getPreference())
                .introduction(myPetProfileCreateRequestDto.getIntroduction())
                .tel(myPetProfileCreateRequestDto.getTel())
                .user(petOwner)
                .build();

        Pet savedMyPetProfile = myPetRepository.save(pet);

        // 이미지 추후 추가

        log.info("내 반려동물 프로필 작성 완료: id={}, title={}", savedMyPetProfile.getId(), savedMyPetProfile.getName());

        return MyPetProfileResponseDto.from(savedMyPetProfile);
    }
    @Transactional
    public List<MyPetProfileResponseDto> getMyPetProfiles(String username) {
        log.info("=== 내 반려동물 프로필 리스트 조회 시작 ===");

        List<Pet> myPetProfiles = myPetRepository.findMyPetProfilesByPetOwner(username);

        log.info("조회된 산책 내 반려동물 프로필 리스트 수 : {}", myPetProfiles.size());

        return myPetProfiles.stream().map(MyPetProfileResponseDto::from).collect(Collectors.toList());
    }
    @Transactional
    public MyPetProfileResponseDto updateMyPetProfile(Long id, MyPetProfileUpdateRequestDto myPetProfileUpdateRequestDto, String username) {
        Pet pet = myPetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("작성된 내 반려동물 프로필이 존재하지 않습니다."));

        if (!pet.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 반려동물 프로필만 수정할 수 있습니다.");
        }

        pet.updateMyPetProfile(
                myPetProfileUpdateRequestDto.getName(),
                myPetProfileUpdateRequestDto.getGender(),
                myPetProfileUpdateRequestDto.getPreference(),
                myPetProfileUpdateRequestDto.getIntroduction()
        );

        // 이미지 수정

        return MyPetProfileResponseDto.from(pet);
    }

    @Transactional
    public void deleteMyPetProfile(Long id, String username) {
        Pet pet = myPetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("내 반려동물 프로필이 존재하지 않습니다."));

        if (!pet.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 반려동물 프로필만 삭제할 수 있습니다.");
        }
        myPetRepository.delete(pet);
    }

    // 내 테일리프렌즈 게시글들 조회
    public Page<MyTailyFriendListResponseDto> getMyTailyFriends(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TailyFriend> postsPage = tailyFriendRepository.findByUserId(user.getId(), pageable);

        return postsPage.map(MyTailyFriendListResponseDto::from); // DTO로 변환
    }

    // 내가 팔로잉하는 유저
    public List<MyFollowUserResponseDto> getFollowingUsers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Follow> followings = followRepository.findActiveByFollowerId(user.getId());
        List<Image> allImages = imageRepository.findAllByTableTypesId(1L); // 모든 프로필 이미지 가져오기

        return followings.stream()
                .map(f -> MyFollowUserResponseDto.from(f.getFollowing(), allImages))
                .collect(Collectors.toList());
    }

    // 나를 팔로우한 유저
    public List<MyFollowUserResponseDto> getFollowerUsers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Follow> followers = followRepository.findActiveByFollowingId(user.getId());
        List<Image> allImages = imageRepository.findAllByTableTypesId(1L);

        return followers.stream()
                .map(f -> MyFollowUserResponseDto.from(f.getFollower(), allImages))
                .collect(Collectors.toList());
    }

    // 팔로잉에서 nickname 검색
    public List<MyFollowUserResponseDto> searchFollowingUsers(String username, String nickname) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Follow> followings = followRepository.findActiveByFollowerId(user.getId())
                .stream()
                .filter(f -> f.getFollowing().getNickname().toLowerCase().contains(nickname.toLowerCase()))
                .collect(Collectors.toList());

        List<Image> allImages = imageRepository.findAllByTableTypesId(1L);

        return followings.stream()
                .map(f -> MyFollowUserResponseDto.from(f.getFollowing(), allImages))
                .collect(Collectors.toList());
    }

    // 팔로워에서 nickname 검색
    public List<MyFollowUserResponseDto> searchFollowerUsers(String username, String nickname) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Follow> followers = followRepository.findActiveByFollowingId(user.getId())
                .stream()
                .filter(f -> f.getFollower().getNickname().toLowerCase().contains(nickname.toLowerCase()))
                .collect(Collectors.toList());

        List<Image> allImages = imageRepository.findAllByTableTypesId(1L);

        return followers.stream()
                .map(f -> MyFollowUserResponseDto.from(f.getFollower(), allImages))
                .collect(Collectors.toList());
    }


    public List<MyLikesResponseDto> getMyLikes(String username) {
        log.info("=== 내 좋아요 리스트 조회 시작 ===");

        List<Like> myLikes = myLikesRepository.findByUserUsernameAndState(username, true);

        List<MyLikesResponseDto> result = new ArrayList<>();

        // 테이블 별 id 분리
        Map<TableTypeCategory, List<Long>> idsByCategory = myLikes.stream()
                .collect(Collectors.groupingBy(
                        like -> like.getTableType().getCategory(),
                        Collectors.mapping(Like::getPostsId, Collectors.toList())
        ));

        // taily friends
        List<Long> tailyFriendsIds = idsByCategory.getOrDefault(TableTypeCategory.TAILY_FRIENDS, Collections.emptyList());

        log.info("조회된 내 좋아요 리스트 수 : {}", myLikes.size());

        return myLikes.stream().map(MyLikesResponseDto::from).collect(Collectors.toList());
    }
}
