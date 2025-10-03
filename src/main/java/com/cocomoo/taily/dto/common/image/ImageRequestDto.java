package com.cocomoo.taily.dto.common.image;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestDto {
    private String uuid;
    private String filePath;
    private String fileSize;
}
