package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.dto.follow.FollowUserResponseDto;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.User;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyFollowUserResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String profileImageUrl; // 프로필 이미지 URL, 없으면 null

    public static MyFollowUserResponseDto from(User user, List<Image> allImages) {
        String imageUrl = allImages.stream()
                .filter(img -> img.getTableTypesId() == 1L && img.getUser() != null && img.getUser().getId().equals(user.getId()))
                .map(Image::getFilePath)
                .findFirst()
                .orElse(null);

        return MyFollowUserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .profileImageUrl(imageUrl)
                .build();
    }
}
