package com.cocomoo.taily.dto.alarm;

import com.cocomoo.taily.entity.Alarm;
import com.cocomoo.taily.entity.AlarmCategory;
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
    private Long postsId;   // 알람이 발생하는 게시글 Id, follow는 null
    private Long senderId;  // 액션을 하는 사람 Id
    private String senderNickname;  // 액션을 하는 사람의 닉네임
    private Long receiverId;    //알람을 받는 유저 Id
    private String receiverNickname;    // 알람 받는 유저의 닉네임
    private Long tableTypeId;   // 카테고리 타입 Id
    private String tableTypeCategory;   // 게시판 이름: TailyFriends, Feed, WalkPath
    private AlarmCategory alarmCategory; // 알람 종류: Comment, Like, follow, Chatting

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
                .senderNickname(alarm.getSender().getNickname())
                .receiverId(alarm.getReceiver().getId())
                .receiverNickname(alarm.getReceiver().getNickname())
                .tableTypeId(alarm.getTableType() != null ? alarm.getTableType().getId() : null)
                .tableTypeCategory(alarm.getTableType().getCategory().getDisplayName())
                .alarmCategory(alarm.getCategory())
                .build();
    }

}
