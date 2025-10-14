package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class MyLikesResponseDto {
    private Long likeId;
    private Long postId;
    private String username;
    private Long tableTypeId;
    private String tableTypeCategory;

    // tailyFriends
    public static MyLikesResponseDto from(Like like) {
        User user = like.getUser();
        TableType tableType = like.getTableType();

        return MyLikesResponseDto.builder()
                .likeId(like.getId())
                .postId(like.getPostsId())
                .username(user.getUsername())
                .tableTypeId(tableType.getId())
                .tableTypeCategory(String.valueOf(tableType.getCategory()))
                .build();
    }
}
