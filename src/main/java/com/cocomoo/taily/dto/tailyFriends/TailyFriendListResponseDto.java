package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.entity.TailyFriend;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TailyFriendListResponseDto {
    private long id;
    private String title;
    private String nickname;
    private Long view;
    private LocalDateTime createdAt;

    public static TailyFriendListResponseDto from(TailyFriend tailyFriend){
        return TailyFriendListResponseDto.builder()
                .id(tailyFriend.getId())
                .title(tailyFriend.getTitle())
                .nickname(tailyFriend.getUser().getNickname())
                .view(tailyFriend.getView())
                .createdAt(tailyFriend.getCreatedAt())
                .build();
    }
}
