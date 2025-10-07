package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.dto.common.image.ImageRequestDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TailyFriendCreateRequestDto {
    private String title;
    private String content;
    private String address;
    private List<ImageRequestDto> images;
}

