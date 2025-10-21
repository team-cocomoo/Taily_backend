package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.entity.WalkPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyWalkPathListResponseDto {
    private Long id;
    private String title;
    private Long view;
    private LocalDateTime createdAt;

    public static MyWalkPathListResponseDto from(WalkPath post) {
        return MyWalkPathListResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .view(post.getView())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
