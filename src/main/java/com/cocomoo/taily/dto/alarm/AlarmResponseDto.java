package com.cocomoo.taily.dto.alarm;

import com.cocomoo.taily.entity.Alarm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmResponseDto {
    private Long id;    // 알람 Id
    private String content; // 알람 내용
    private boolean state;  // 읽음 여부
    private LocalDateTime createdAt;
    private Long postsId;   // 댓글이 달린 게시글 Id
    private Long senderId;  // 액션을 하는 사람 Id
    private String senderName;  // 액션을 하는 사람의 아이디
    private Long receiverId;    //알람을 받는 유저 Id
    private Long tableTypeId;   // 카테고리 타입 Id
    private String tableTypeCategory;   // Comment, Like, follow ...

    /**
     * Alarm → AlarmResponseDto 변환
     */
    public static AlarmResponseDto from(Alarm alarm) {
        return AlarmResponseDto.builder()
                .id(alarm.getId())
                .content(alarm.getContent())
                .state(alarm.getState())
                .createdAt(alarm.getCreatedAt())
                .postsId(alarm.getPostsId())
                .senderId(alarm.getSender().getId())
                .senderName(alarm.getSender().getUsername())
                .receiverId(alarm.getReceiver().getId())
                .tableTypeId(alarm.getTableTypeId().getId())
                .tableTypeCategory(alarm.getTableTypeId().getCategory().getDisplayName())
                .build();
    }

}
