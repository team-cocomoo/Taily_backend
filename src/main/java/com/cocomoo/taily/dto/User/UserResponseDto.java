package com.cocomoo.taily.dto.User;

import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDto {
    private Long id; // 회원 정보(pk)
    private String publicId; // 외부로 노출되는 아이디
    private String username; // 로그인 ID (Entity 필드명과 일치)
    private String nickname; // 닉네임
    private String password; // 비밀번호
    private String tel; // 전화번호
    private String email; // 이메일
    private String address; // 주소
    private String introduction; // 자기 소개
    private UserRole role; // 회원 종류
    private String state; // 회원 상태
    private LocalDateTime createdAt; // 회원 생성일
    private LocalDateTime updatedAt; // 회원 정보 업데이트 날짜
    private TableType tableTypeId; // 테이블 아이디

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .password(user.getPassword())
                .tel(user.getTel())
                .email(user.getEmail())
                .address(user.getAddress())
                .introduction(user.getIntroduction())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .tableTypeId(user.getTableType())
                .build();

    }

}
