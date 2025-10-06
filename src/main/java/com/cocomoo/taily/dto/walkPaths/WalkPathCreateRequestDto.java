package com.cocomoo.taily.dto.walkPaths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 산책경로 게시물 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkPathCreateRequestDto {
    private String title;
    private String content;
    private Long userId;

}
