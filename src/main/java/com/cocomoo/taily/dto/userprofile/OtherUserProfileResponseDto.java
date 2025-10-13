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
    private String publicId;

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
            List<Image> feedImageList,
            List<Image> petImageList
    ) {
        List<PetInfoResponseDto> petDtos = petList.stream()
                .map(pet -> {
                    // 해당 반려동물의 이미지 찾기 (imageList에서 tableTypeId == 2 && postsId == pet.id 등 조건)
                    String petImageUrl = petImageList.stream()
                            .filter(img -> img.getTableType().getId() == 2 && img.getPostsId().equals(pet.getId()))
                            .map(Image::getFilePath)
                            .findFirst()
                            .orElse(null);

                    return PetInfoResponseDto.builder()
                            .name(pet.getName())
                            .gender(pet.getGender())
                            .preference(pet.getPreference())
                            .introduction(pet.getIntroduction())
                            .createdAt(pet.getCreatedAt())
                            .imageUrl(petImageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        List<FeedImageResponseDto> feedDtos = feedList.stream()
                .map(feed -> FeedImageResponseDto.builder()
                        .feedId(feed.getId())
                        .createdAt(feed.getCreatedAt())
                        .imageUrls(
                                feedImageList.stream()
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
                .publicId(user.getPublicId())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .postCount(postCount)
                .pets(petDtos)
                .feeds(feedDtos)
                .build();
    }
}
