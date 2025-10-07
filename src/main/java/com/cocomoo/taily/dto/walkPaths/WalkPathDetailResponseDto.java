package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.entity.TableTypeCategory;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkPath;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 산책경로 게시물 응답 Dto
@Getter
@Builder
public class WalkPathDetailResponseDto {
    private Long postId;
    private String title;
    private String content;
    private Long view;
    private boolean liked;
    private Long likeCount;
    private List<ImageResponseDto> images;
    private TableTypeCategory category;
    private Long user;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;


    // Entity -> Dto 변환 메서드
    public static WalkPathDetailResponseDto from(WalkPath walkPath, boolean liked ,List<ImageResponseDto> images){
        User user = walkPath.getUser();
        return WalkPathDetailResponseDto.builder()
                .postId(walkPath.getId())
                .title(walkPath.getTitle())
                .content(walkPath.getContent())
                .view(walkPath.getView())
                .likeCount(walkPath.getLikeCount())
                .liked(liked)
                .images(images)
                .user(user.getId())
                .authorName(user.getUsername())
                .authorEmail(user.getEmail())
                .createdAt(walkPath.getCreatedAt())
                .build();
    }
}
