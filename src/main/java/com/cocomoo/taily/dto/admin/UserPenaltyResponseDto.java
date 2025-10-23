package com.cocomoo.taily.dto.admin;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPenaltyResponseDto {
    private Long userId;
    private String username;
    private String nickname;
    private UserState state;
    private LocalDateTime penaltyStartDate;
    private LocalDateTime penaltyEndDate;

    public static UserPenaltyResponseDto from(User user) {
        return UserPenaltyResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .state(user.getState())
                .penaltyStartDate(user.getPenaltyStartDate())
                .penaltyEndDate(user.getPenaltyEndDate())
                .build();
    }
}
