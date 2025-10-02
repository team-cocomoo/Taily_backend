package com.cocomoo.taily.dto.walkDiary;

import com.cocomoo.taily.entity.WalkDiary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalkDiaryListResponseDto {
    private Long id;
    private String content;
    private String userNickname;
    private LocalDateTime createdAt;

    public static WalkDiaryListResponseDto from(WalkDiary walkDiary) {
        return WalkDiaryListResponseDto.builder()
                .id(walkDiary.getId())
                .content(walkDiary.getContent())
                .userNickname(walkDiary.getContent())
                .createdAt(walkDiary.getCreatedAt())
                .build();
    }
}
