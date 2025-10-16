package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.dto.common.image.ImageRequestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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
    private List<WalkPathRouteRequestDto> routes;
    private List<MultipartFile> images;

}
