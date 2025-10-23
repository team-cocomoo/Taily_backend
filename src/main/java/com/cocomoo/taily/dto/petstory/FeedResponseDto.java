package com.cocomoo.taily.dto.petstory;

import com.cocomoo.taily.entity.Feed;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.TagList;
import com.cocomoo.taily.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 피드 응답 DTO (이미지 경로 기반 + 작성자 PK 제거)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponseDto {

    private Long id;                  // 피드 pk
    private String content;           // 피드 내용
    private Long view;                // 조회수
    private Long likeCount;           // 좋아요 수
    private LocalDateTime createdAt;  // 작성일
    private LocalDateTime updatedAt;  // 수정일
    private List<String> images;      // 이미지 경로 리스트
    private List<String> tags;        // 태그 리스트
    private String writerNickName;    // 작성자 닉네임
    private String writerPublicId;    // 작성자 공개 ID
    private Long writerId;              // 작성자 pk

    /**
     * Feed → DTO 변환 메서드 (이미지 경로 기반)
     */
    public static FeedResponseDto of(
            Feed feed,
            List<Image> imageList,
            List<TagList> tagList
    ) {
        return FeedResponseDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .view(feed.getView())
                .likeCount(feed.getLikeCount())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .images(imageList.stream()
                        .map(Image::getFilePath)
                        .collect(Collectors.toList()))
                .tags(tagList.stream()
                        .map(t -> t.getTag().getName())
                        .collect(Collectors.toList()))
                .writerNickName(feed.getUser().getNickname())
                .writerPublicId(feed.getUser().getPublicId())
                .writerId(feed.getUser().getId())
                .build();
    }

    /**
     * Feed → DTO 변환 (User 명시적 전달 버전)
     * Lazy 로딩 방지용으로 userRepository에서 직접 조회한 User를 전달 가능
     */
    public static FeedResponseDto of(
            Feed feed,
            List<Image> imageList,
            List<TagList> tagList,
            User user
    ) {
        return FeedResponseDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .view(feed.getView())
                .likeCount(feed.getLikeCount())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .images(imageList.stream()
                        .map(Image::getFilePath)
                        .collect(Collectors.toList()))
                .tags(tagList.stream()
                        .map(t -> t.getTag().getName())
                        .collect(Collectors.toList()))
                .writerNickName(user != null ? user.getNickname() : "익명")
                .writerPublicId(user != null ? user.getPublicId() : null)
                .writerId(user != null ? user.getId() : null)
                .build();
    }
}
