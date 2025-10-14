package com.cocomoo.taily.dto.chat;

import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChatSearchResponseDto {
    private Long id;
    private String username;
    private String publicId;
    private String profileImage;

    // User 엔티티와 프로필 이미지 경로로 DTO 생성
    public static UserChatSearchResponseDto from(User user, String profileImage) {
        return UserChatSearchResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .publicId(user.getPublicId())
                .profileImage(profileImage)
                .build();
    }
}