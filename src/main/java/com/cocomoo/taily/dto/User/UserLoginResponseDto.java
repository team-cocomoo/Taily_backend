package com.cocomoo.taily.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponseDto {
    private String username;
    private String nickname;
    private String role;
    private String token; // JWT 토큰
}