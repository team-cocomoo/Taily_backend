package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.dto.common.image.ImageRequestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

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
    private List<WalkPathRouteRequestDto> route;
    //private Long userId;
    private List<ImageRequestDto> images;

}
