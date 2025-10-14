package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.entity.TailyFriend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyTailyFriendListResponseDto {
    private Long id;
    private String title;
    private Long view;
    private LocalDateTime createdAt;

    public static MyTailyFriendListResponseDto from(TailyFriend post) {
        return MyTailyFriendListResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .view(post.getView())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
