package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDto {
    private String publicId; // 외부로 노출되는 아이디
    private String username; // 로그인 ID (Entity 필드명과 일치)
    private String nickname; // 닉네임
    private String tel; // 전화번호
    private String email; // 이메일
    private String address; // 주소
    private String introduction; // 자기 소개

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .tel(user.getTel())
                .email(user.getEmail())
                .address(user.getAddress())
                .introduction(user.getIntroduction())
                .build();
    }
}