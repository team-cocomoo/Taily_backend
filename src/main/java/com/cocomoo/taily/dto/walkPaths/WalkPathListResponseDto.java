package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkPath;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalkPathListResponseDto{
    private Long id;
    private String title;
    private Long view;
    private LocalDateTime createdAt;
    private User user;

    //Entity -> Dto
    public static WalkPathListResponseDto from(WalkPath walkPath){
        return WalkPathListResponseDto.builder()
                .id(walkPath.getId())
                .title(walkPath.getTitle())
                .view(walkPath.getView())
                .createdAt(walkPath.getCreatedAt())
                .user(walkPath.getUser())
                .build();
    }
}
