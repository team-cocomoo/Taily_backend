package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.entity.TailyFriend;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TailyFriendListResponseDto {
    private long id;
    private String title;
    private String nickname;
    private Long view;
    private List<ImageResponseDto> images;
    private LocalDateTime createdAt;


    public static TailyFriendListResponseDto from(TailyFriend tailyFriend, List<ImageResponseDto> images){
        return TailyFriendListResponseDto.builder()
                .id(tailyFriend.getId())
                .title(tailyFriend.getTitle())
                .nickname(tailyFriend.getUser().getNickname())
                .view(tailyFriend.getView())
                .images(images)
                .createdAt(tailyFriend.getCreatedAt())
                .build();
    }
}
