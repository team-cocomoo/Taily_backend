package com.cocomoo.taily.dto.follow;

import com.cocomoo.taily.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowResponseDto {
    private Long id;
    private Long followerId;
    private Long followingId;

    public static FollowResponseDto from(Follow follow) {
        return FollowResponseDto.builder()
                .id(follow.getId())
                .followerId(follow.getFollower().getId())
                .followingId(follow.getFollowing().getId())
                .build();
    }
}
