package com.cocomoo.taily.dto.userprofile;

import com.cocomoo.taily.dto.myPage.UserProfileResponseDto;
import com.cocomoo.taily.entity.Feed;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.Pet;
import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUserProfileResponseDto {
    // 기본 유저 정보
    private Long id;
    private String nickname;
    private String introduction;

    // 통계 정보
    private Long followerCount;
    private Long followingCount;
    private Long postCount;

    // 반려동물 정보
    private List<PetInfoResponseDto> pets;

    // 작성한 피드 목록
    private List<FeedImageResponseDto> feeds;

    public static OtherUserProfileResponseDto from(
            User user,
            Long followerCount,
            Long followingCount,
            Long postCount,
            List<Pet> petList,
            List<Feed> feedList,
            List<Image> imageList
    ) {
        List<PetInfoResponseDto> petDtos = petList.stream()
                .map(pet -> PetInfoResponseDto.builder()
                        .name(pet.getName())
                        .gender(pet.getGender())
                        .preference(pet.getPreference())
                        .introduction(pet.getIntroduction())
                        .createdAt(pet.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<FeedImageResponseDto> feedDtos = feedList.stream()
                .map(feed -> FeedImageResponseDto.builder()
                        .feedId(feed.getId())
                        .createdAt(feed.getCreatedAt())
                        .imageUrls(
                                imageList.stream()
                                        .filter(img -> img.getPostsId().equals(feed.getId())) // feed.id == image.posts_id
                                        .map(Image::getFilePath)
                                        .collect(Collectors.toList())
                        )
                        .build())
                .collect(Collectors.toList());

        return OtherUserProfileResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .introduction(user.getIntroduction())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .postCount(postCount)
                .pets(petDtos)
                .feeds(feedDtos)
                .build();
    }
}
