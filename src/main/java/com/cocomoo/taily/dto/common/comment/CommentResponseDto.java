package com.cocomoo.taily.dto.common.comment;

import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.entity.TailyFriend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private String nickname;
    private String profileImage;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> replies;

    public static CommentResponseDto from (Comment comment, String profileImagePath){
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUsersId().getNickname())
                .profileImage(profileImagePath)
                .createdAt(comment.getCreatedAt())
                .replies(new ArrayList<>())
                .build();
    }

    public static CommentResponseDto fromWithReplies(Comment comment, List<Comment> allComments, Map<Long, String> profileMap) {
        List<CommentResponseDto> childReplies = allComments.stream()
                .filter(c -> c.getParentCommentsId() != null && c.getParentCommentsId().getId().equals(comment.getId()))
                .map(c -> fromWithReplies(c, allComments, profileMap))
                .collect(Collectors.toList());

        String profileImagePath = profileMap.get(comment.getUsersId().getId());

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUsersId().getNickname())
                .profileImage(profileImagePath)
                .createdAt(comment.getCreatedAt())
                .replies(childReplies)
                .build();
    }
}
