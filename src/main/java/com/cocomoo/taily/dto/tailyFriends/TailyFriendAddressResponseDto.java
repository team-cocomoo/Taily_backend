package com.cocomoo.taily.dto.tailyFriends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TailyFriendAddressResponseDto {
    private String address;

    public static TailyFriendAddressResponseDto from(String address) {
        return TailyFriendAddressResponseDto.builder()
                .address(address)
                .build();
    }
}
