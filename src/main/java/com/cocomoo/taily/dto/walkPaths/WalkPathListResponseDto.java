package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkPath;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WalkPathListResponseDto{
    private Long id;
    private String title;
    private Long view;
    private LocalDateTime createdAt;
    private List<ImageResponseDto> images;
    private String nickname;

    //Entity -> Dto
    public static WalkPathListResponseDto from(WalkPath walkPath,List<ImageResponseDto> images){
        return WalkPathListResponseDto.builder()
                .id(walkPath.getId())
                .title(walkPath.getTitle())
                .view(walkPath.getView())
                .images(images)
                .createdAt(walkPath.getCreatedAt())
                .nickname(walkPath.getUser() != null ? walkPath.getUser().getNickname() : null)
                .build();
    }
}
