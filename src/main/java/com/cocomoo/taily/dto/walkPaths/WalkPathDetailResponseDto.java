package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkPath;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 산책경로 게시물 응답 Dto
@Getter
@Builder
public class WalkPathDetailResponseDto {
    private Long postId;
    private String title;
    private String content;
    private Long user;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;

    // Entity -> Dto 변환 메서드
    public static WalkPathDetailResponseDto from(WalkPath walkPath){
        User user = walkPath.getUser();
        return WalkPathDetailResponseDto.builder()
                .postId(walkPath.getId())
                .title(walkPath.getTitle())
                .content(walkPath.getContent())
                .user(user.getId())
                .authorName(user.getUsername())
                .authorEmail(user.getEmail())
                .createdAt(walkPath.getCreatedAt())
                .build();
    }
}
