package com.cocomoo.taily.dto.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 정보 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String username;
    private String nickname;
    private String password;
    private String tel;
    private String email;
    private String address;
    private String introduction;
    private String state;
    // 데이터 타입 맞춰야 한다.
}
