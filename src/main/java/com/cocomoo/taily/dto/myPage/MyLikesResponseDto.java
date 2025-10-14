package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyLikesResponseDto {
    private Long likeId;
    private Long postId;
    private String username;
    private Long tableTypeId;
    private String tableTypeCategory;
    private String targetName;  // 내가 좋아요를 누른 게시글의 작성자

    public static MyLikesResponseDto from(Like like) {
        User user = like.getUser();
        TableType tableType = like.getTableType();

        return MyLikesResponseDto.builder()
                .likeId(like.getId())
                .postId(like.getPostsId())
                .username(user.getUsername())
                .tableTypeId(tableType.getId())
                .tableTypeCategory(String.valueOf(tableType.getCategory()))
                .targetName(user.getUsername())
                .build();
    }
}
