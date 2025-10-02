package com.cocomoo.taily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 가입 요청 Dto
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DummyDto {
    private String username;
    private String password;
    private String nickname;
    private String tel;
    private String email;
    private String address;
    private String introduction;
}
