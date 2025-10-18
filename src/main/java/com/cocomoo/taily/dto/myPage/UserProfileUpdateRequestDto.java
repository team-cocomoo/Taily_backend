package com.cocomoo.taily.dto.myPage;

import lombok.Getter;
import lombok.Setter;

/**
 * 유저 업데이트 요청용 Dto(Data Transfer Object)
 * 아이디, 닉네임, 전화번호, 이메일, 주소, 소개글, 비밀번호 변경 가능
 */
@Getter
@Setter
public class UserProfileUpdateRequestDto {
    private Long id;             // pk 값
    private String username;     // 로그인 ID
    private String nickname;     // 닉네임
    private String tel;          // 전화번호
    private String email;        // 이메일
    private String address;      // 주소
    private String introduction; // 자기소개
    private String newPassword;  // 변경할 비밀번호 (선택적)
}
