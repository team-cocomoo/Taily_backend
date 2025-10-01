package com.cocomoo.taily.dto.tailyFriends;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TailyFriendDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private String address;
    private Long view;
    private Long likeCount;
    private Long authorId;
    private String authorUsername;
    private String authorName;
    private LocalDateTime createdAt;
}
