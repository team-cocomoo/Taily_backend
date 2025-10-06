package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.dto.User.UserResponseDto;
import com.cocomoo.taily.entity.User;

/**
 * 유저 업데이트 요청용 Dto(Data Transfer Object)
 * 아이디, 닉네임, 전화번호, 이메일, 주소, 소개글 변경 가능하므로 이 객체에 정보를 담아서 전송
 */
public class UserProfileUpdateRequestDto {
    private Long id; // pk 값
    private String username; // 로그인 id
    private String nickname; // 닉네임
    private String tel; // 전화번호
    private String email; // 이메일
    private String address; // 주소
    private String introduction; // 자기소개
    private String newPassword; // 변경할 비밀번호
}