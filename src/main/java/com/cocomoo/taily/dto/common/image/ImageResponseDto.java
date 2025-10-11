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
}
