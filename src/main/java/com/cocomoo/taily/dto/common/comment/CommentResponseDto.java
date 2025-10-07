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
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> replies;

    public static CommentResponseDto from (Comment comment){
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(comment.getUsersId().getUsername())
                .createdAt(comment.getCreatedAt())
                .replies(new ArrayList<>())
                .build();
    }

    public static CommentResponseDto fromWithReplies(Comment comment, List<Comment> allComments) {
        List<CommentResponseDto> childReplies = allComments.stream()
                .filter(c -> c.getParentCommentsId() != null && c.getParentCommentsId().getId().equals(comment.getId()))
                .map(c -> fromWithReplies(c, allComments)) // 재귀 호출
                .collect(Collectors.toList());

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(comment.getUsersId().getUsername())
                .createdAt(comment.getCreatedAt())
                .replies(childReplies)
                .build();
    }
}
