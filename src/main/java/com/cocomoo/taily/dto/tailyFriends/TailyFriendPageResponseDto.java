package com.cocomoo.taily.dto.tailyFriends;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TailyFriendPageResponseDto {
    private List<TailyFriendListResponseDto> data;
    private long totalCount;
}
