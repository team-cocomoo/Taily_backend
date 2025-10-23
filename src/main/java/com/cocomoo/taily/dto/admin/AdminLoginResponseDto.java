package com.cocomoo.taily.dto.admin;

import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdminLoginResponseDto {
    private String username;
    private String nickname;
    private String role;
    private String token; // JWT 토큰

    public static AdminLoginResponseDto from(User user, String token) {
        return AdminLoginResponseDto.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .token(token)
                .build();
    }
}
