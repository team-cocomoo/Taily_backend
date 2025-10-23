package com.cocomoo.taily.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 * JSON 형식의 로그인 요청을 매핑
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {
    private String username;
    private String password;
}
