package com.cocomoo.taily.dto.userprofile;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.entity.TailyFriend;
import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSummaryResponseDto {
    private Long id;                // 사용자 ID
    private String nickname;        // 닉네임
    private Long followerCount;     // 팔로워 수
    private Long followingCount;    // 팔로잉 수
    private Long postCount;         // 작성 피드 수

    public static UserProfileSummaryResponseDto from(User user,
                                                     Long followerCount,
                                                     Long followingCount,
                                                     Long postCount) {
        return UserProfileSummaryResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .postCount(postCount)
                .build();
    }
}
