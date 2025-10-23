package com.cocomoo.taily.dto.admin;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserState;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private LocalDateTime createdAt;
    private UserState state;
    private Long sanctionCount;
    private String imagePath;
    private LocalDateTime penaltyStartDate;
    private LocalDateTime penaltyEndDate;

    public static UserListResponseDto from(User user, String imagePath) {
        return UserListResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .state(user.getState())
                .sanctionCount(user.getSanctionCount())
                .imagePath(imagePath)
                .penaltyStartDate(user.getPenaltyStartDate())
                .penaltyEndDate(user.getPenaltyEndDate())
                .build();
    }
}
