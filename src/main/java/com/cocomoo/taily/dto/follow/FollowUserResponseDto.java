package com.cocomoo.taily.dto.follow;

import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowUserResponseDto {
    private Long userId;
    private String username;

    public static FollowUserResponseDto from(User user) {
        return FollowUserResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
