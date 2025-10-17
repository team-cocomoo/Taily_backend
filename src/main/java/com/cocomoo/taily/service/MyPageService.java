package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.myPage.*;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 유저 정보 조회 메서드
    @Transactional(readOnly = true)
    public UserProfileResponseDto getMyInfo(String publicId) {
        log.info("내 정보 조회 요청: publicId={}", publicId);

        // DB에서 유저 조회
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return UserProfileResponseDto.from(user);
    }

    // 유저 정보 수정 메서드
    @Transactional
    public UserProfileResponseDto updateMyInfo(String publicId, UserProfileUpdateRequestDto dto) {
        log.info("내 정보 수정 요청: publicId={}, dto={}", publicId, dto);

        // DB에서 유저 조회
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경 로직 (선택적)
        String encodedPassword = null;
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(dto.getNewPassword());
            log.info("비밀번호 변경 요청 감지: publicId={}", publicId);
        }

        // User 엔티티 업데이트 (비밀번호 없을 때는 기존 유지)
        user.updateInfo(
                dto.getUsername(),            // username
                dto.getNickname(),            // nickname
                encodedPassword,              // password (null이면 updateInfo에서 변경 안 됨)
                dto.getTel(),                 // tel
                dto.getEmail(),               // email
                dto.getAddress(),             // address
                dto.getIntroduction(),        // introduction
                null                          // 일반 유저는 state 변경 불가
        );

        // 변경 저장
        userRepository.save(user);

        log.info("내 정보 수정 완료: nickname={}, tel={}, email={}",
                user.getNickname(), user.getTel(), user.getEmail());

        // 응답 DTO 반환
        return UserProfileResponseDto.from(user);
    }



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


        return MyPetProfileResponseDto.from(savedMyPetProfile);
    }
    @Transactional
    public List<MyPetProfileResponseDto> getMyPetProfiles(String username) {
        List<Pet> myPetProfiles = myPetRepository.findMyPetProfilesByPetOwner(username);

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


//    public List<MyLikesResponseDto> getMyLikes(String username) {
//        log.info("=== 내 좋아요 리스트 조회 시작 ===");
//
//        List<Like> myLikes = myLikesRepository.findByUserUsernameAndStateOrderByIdDesc(username, true);
//
//        List<MyLikesResponseDto> result = new ArrayList<>();
//
//        // 테이블 별 id 분리
//        Map<TableTypeCategory, List<Long>> idsByCategory = myLikes.stream()
//                .collect(Collectors.groupingBy(
//                        like -> like.getTableType().getCategory(),
//                        Collectors.mapping(Like::getPostsId, Collectors.toList())
//                ));
//
//        // taily friends
//        List<Long> tailyFriendsIds = idsByCategory.getOrDefault(TableTypeCategory.TAILY_FRIENDS, Collections.emptyList());
//        if (!tailyFriendsIds.isEmpty()) {
//            List<TailyFriend> friends = tailyFriendRepository.findAllByIdIn(tailyFriendsIds);
//            friends.forEach(f -> {
//                Like like = myLikes.stream().filter(l -> l.getPostsId().equals(f.getId()))
//                        .findFirst().orElse(null);
//                if (like != null) {
//                    result.add(
//                            MyLikesResponseDto.builder()
//                                    .likeId(like.getId())
//                                    .postId(like.getPostsId())
//                                    .username(like.getUser().getUsername())
//                                    .tableTypeId(like.getTableType().getId())
//                                    .tableTypeCategory(like.getTableType().getCategory().getDisplayName())
//                                    .targetName(f.getUser().getUsername())
//                                    .build());
//                }
//            });
//        }
//
//        // Feed
//        List<Long> feedIds = idsByCategory.getOrDefault(TableTypeCategory.FEEDS, Collections.emptyList());
//        if (!feedIds.isEmpty()) {
//            List<Feed> feeds = feedRepository.findAllByIdIn(feedIds);
//            feeds.forEach(feed -> {
//                Like like = myLikes.stream()
//                        .filter(l -> l.getPostsId().equals(feed.getId()))
//                        .findFirst()
//                        .orElse(null);
//                if (like != null) {
//                    result.add(MyLikesResponseDto.builder()
//                            .likeId(like.getId())
//                            .postId(like.getPostsId())
//                            .username(like.getUser().getUsername())
//                            .tableTypeId(like.getTableType().getId())
//                            .tableTypeCategory(like.getTableType().getCategory().getDisplayName())
//                            .targetName(feed.getUser().getUsername())
//                            .build()
//                    );
//                }
//            });
//        }
//
//        // walkPath
//        List<Long> walkPathIds = idsByCategory.getOrDefault(TableTypeCategory.WALK_PATHS, Collections.emptyList());
//        if (!walkPathIds.isEmpty()) {
//            List<WalkPath> walks = walkPathRepository.findAllByIdIn(walkPathIds);
//            walks.forEach(walkPath -> {
//                Like like = myLikes.stream()
//                        .filter(l -> l.getPostsId().equals(walkPath.getId()))
//                        .findFirst()
//                        .orElse(null);
//                if (like != null) {
//                    result.add(MyLikesResponseDto.builder()
//                            .likeId(like.getId())
//                            .postId(like.getPostsId())
//                            .username(like.getUser().getUsername())
//                            .tableTypeId(like.getTableType().getId())
//                            .tableTypeCategory(like.getTableType().getCategory().getDisplayName())
//                            .targetName(walkPath.getUser().getUsername())
//                            .build());
//                }
//            });
//        }
//
//        log.info("조회된 내 좋아요 리스트 수 : {}", myLikes.size());
//
//        return result;
//    }

    @Transactional
    public boolean toggleLike(String username, Long postsId, Long tableTypeId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        TableType tableType = tableTypeRepository.findById(tableTypeId).orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        Like like = myLikesRepository.findByUserAndPostsIdAndTableType(user, postsId, tableType).orElseGet(() -> Like.builder()
                .user(user)
                .postsId(postsId)
                .tableType(tableType)
                .state(false)
                .build());

        like.toggle();   // 기존 state 반전
        myLikesRepository.save(like);

        return like.isState();  // 토글 후 상태 반환
    }

    public MyLikesPageResponseDto getMyLikesPage(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Like> likesPage = myLikesRepository.findByUserUsernameAndStateOrderByIdDesc(username, true, pageable);

        List<Like> myLikes = likesPage.getContent();    // 실제 페이지 내용

        // 테이블 별 id 분리
        Map<TableTypeCategory, List<Long>> idsByCategory = myLikes.stream()
                .collect(Collectors.groupingBy(
                        like -> like.getTableType().getCategory(),
                        Collectors.mapping(Like::getPostsId, Collectors.toList())
                ));

        List<MyLikesResponseDto> result = new ArrayList<>();

        // 카테고리결 게시글 한 번만 조회 후 map 생성
        Map<Long, TailyFriend> tailyFriendMap = tailyFriendRepository.findAllByIdIn(
                idsByCategory.getOrDefault(TableTypeCategory.TAILY_FRIENDS, Collections.emptyList())
        ).stream().collect(Collectors.toMap(TailyFriend::getId, tailyFriend -> tailyFriend));

        Map<Long, Feed> feedMap = feedRepository.findAllByIdIn(
                idsByCategory.getOrDefault(TableTypeCategory.FEEDS, Collections.emptyList())
        ).stream().collect(Collectors.toMap(Feed::getId, feed -> feed));

        Map<Long, WalkPath> walkMap = walkPathRepository.findAllByIdIn(
                idsByCategory.getOrDefault(TableTypeCategory.WALK_PATHS, Collections.emptyList())
        ).stream().collect(Collectors.toMap(WalkPath::getId, walkPath -> walkPath));


        // 좋아요 + 게시글 매핑
        for (Like like : myLikes) {
            TableTypeCategory category = like.getTableType().getCategory();
            switch (category) {
                case TAILY_FRIENDS -> {
                    TailyFriend tf = tailyFriendMap.get(like.getPostsId());
                    if (tf != null) {
                        result.add(MyLikesResponseDto.builder()
                                .likeId(like.getId())
                                .postId(like.getPostsId())
                                .username(like.getUser().getUsername())
                                .tableTypeId(like.getTableType().getId())
                                .tableTypeCategory(category.getDisplayName())
                                .targetName(tf.getUser().getUsername())
                                .build());
                    }
                }
                case FEEDS -> {
                    Feed f = feedMap.get(like.getPostsId());
                    if (f != null) {
                        result.add(MyLikesResponseDto.builder()
                                .likeId(like.getId())
                                .postId(like.getPostsId())
                                .username(like.getUser().getUsername())
                                .tableTypeId(like.getTableType().getId())
                                .tableTypeCategory(category.getDisplayName())
                                .targetName(f.getUser().getUsername())
                                .build());
                    }
                }
                case WALK_PATHS -> {
                    WalkPath w = walkMap.get(like.getPostsId());
                    if (w != null) {
                        result.add(MyLikesResponseDto.builder()
                                .likeId(like.getId())
                                .postId(like.getPostsId())
                                .username(like.getUser().getUsername())
                                .tableTypeId(like.getTableType().getId())
                                .tableTypeCategory(category.getDisplayName())
                                .targetName(w.getUser().getUsername())
                                .build());
                    }
                }
            }
        }

        log.info("조회된 내 좋아요 리스트 수 : {}", myLikes.size());

        return MyLikesPageResponseDto.builder()
                .myLikeList(result)
                .page(page)
                .size(size)
                .totalCount(likesPage.getTotalElements())
                .totalPages(likesPage.getTotalPages())
                .isLast(likesPage.isLast())
                .build();
    }
}
