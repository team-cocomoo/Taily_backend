package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.entity.TableTypeCategory;
import com.cocomoo.taily.entity.TailyFriend;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TableTypeCategory category;
    private Long userId;
    private String nickname;
    private boolean liked;

    public static TailyFriendDetailResponseDto from(TailyFriend tailyFriend, boolean liked){
        return TailyFriendDetailResponseDto.builder()
                .id(tailyFriend.getId())
                .title(tailyFriend.getTitle())
                .address(tailyFriend.getAddress())
                .content(tailyFriend.getContent())
                .view(tailyFriend.getView())
                .likeCount(tailyFriend.getLikeCount())
                .liked(liked)
                .createdAt(tailyFriend.getCreatedAt())
                .updatedAt(tailyFriend.getUpdatedAt())
                .category(tailyFriend.getTableType().getCategory())
                .userId(tailyFriend.getUser().getId())
                .nickname(tailyFriend.getUser().getNickname())
                .build();
    }
}
