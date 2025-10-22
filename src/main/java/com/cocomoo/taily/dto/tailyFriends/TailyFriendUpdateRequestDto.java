package com.cocomoo.taily.dto.tailyFriends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TailyFriendUpdateRequestDto {
    private String title;
    private String address;
    private String content;
    private List<String> existingImagePaths;
}
