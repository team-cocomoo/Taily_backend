package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.dto.common.image.ImageRequestDto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TailyFriendCreateRequestDto {
    private String title;
    private String address;
    private String content;
    private List<MultipartFile> images;
    private Long tableTypeId;

    public TailyFriendCreateRequestDto withImages(List<MultipartFile> newImages) {
        return this.toBuilder()
                .images(newImages)
                .build();
    }
}

