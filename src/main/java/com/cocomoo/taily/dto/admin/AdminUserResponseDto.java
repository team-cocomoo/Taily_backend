package com.cocomoo.taily.dto.admin;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserState;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자가 회원 정보 조회할 Dto
 */
@Getter
@Builder
public class AdminUserResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String tel;
    private String email;
    private String address;
    private String introduction;
    private UserState state;
    private Long sanctionCount;
    private LocalDateTime penaltyStartDate;
    private LocalDateTime penaltyEndDate;
    private LocalDateTime createdAt;
    private String imagePath;  // 이미지 경로 리스트


    public static AdminUserResponseDto from(User user, String imagePath) {
        return AdminUserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .tel(user.getTel())
                .email(user.getEmail())
                .address(user.getAddress())
                .introduction(user.getIntroduction())
                .state(user.getState())
                .sanctionCount(user.getSanctionCount())
                .penaltyStartDate(user.getPenaltyStartDate())
                .penaltyEndDate(user.getPenaltyEndDate())
                .createdAt(user.getCreatedAt())
                .imagePath(imagePath)
                .build();
    }

}
