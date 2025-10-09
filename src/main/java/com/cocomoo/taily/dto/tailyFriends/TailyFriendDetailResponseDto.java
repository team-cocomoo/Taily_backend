package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.entity.TableTypeCategory;
import com.cocomoo.taily.entity.TailyFriend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TailyFriendDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private String address;
    private Long view;
    private Long likeCount;
    private List<ImageResponseDto> images;
    private LocalDateTime createdAt;
    private TableTypeCategory category;
    private Long userId;
    private String nickname;
    private boolean liked;

    public static TailyFriendDetailResponseDto from(TailyFriend tailyFriend, boolean liked, List<ImageResponseDto> images){
        return TailyFriendDetailResponseDto.builder()
                .id(tailyFriend.getId())
                .title(tailyFriend.getTitle())
                .address(tailyFriend.getAddress())
                .content(tailyFriend.getContent())
                .view(tailyFriend.getView())
                .likeCount(tailyFriend.getLikeCount())
                .liked(liked)
                .images(images)
                .createdAt(tailyFriend.getCreatedAt())
                .category(tailyFriend.getTableType().getCategory())
                .userId(tailyFriend.getUser().getId())
                .nickname(tailyFriend.getUser().getNickname())
                .build();
    }
}
