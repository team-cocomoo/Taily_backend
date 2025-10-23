package com.cocomoo.taily.dto.tailyFriends;

import com.cocomoo.taily.entity.TailyFriend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TailyFriendAddressResponseDto {
    private Long id;
    private String title;
    private String address;
    private String nickname;

    public static TailyFriendAddressResponseDto from(TailyFriend entity) {
        return TailyFriendAddressResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .address(entity.getAddress())
                .nickname(entity.getUser().getNickname())
                .build();
    }
}
