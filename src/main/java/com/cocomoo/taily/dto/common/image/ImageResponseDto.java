package com.cocomoo.taily.dto.common.image;

import com.cocomoo.taily.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDto {
    private Long id;
    private String uuid;
    private String filePath;
    private String fileSize;

    public static ImageResponseDto from(Image image, String baseUrl) {
        return ImageResponseDto.builder()
                .id(image.getId())
                .uuid(image.getUuid())
                .filePath(baseUrl + image.getFilePath())
                .fileSize(image.getFileSize())
                .build();
    }

    // 람다식형태로 호출 오류로 메개변수 하나인 메서드 하나 추가
    // baseUrl이 필요 없는 경우
    public static ImageResponseDto from(Image image) {
        return ImageResponseDto.builder()
                .id(image.getId())
                .uuid(image.getUuid())
                .filePath(image.getFilePath())
                .fileSize(image.getFileSize())
                .build();
    }
}
