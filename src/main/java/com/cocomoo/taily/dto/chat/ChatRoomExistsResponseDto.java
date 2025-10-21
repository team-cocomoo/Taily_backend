package com.cocomoo.taily.dto.chat;

import com.cocomoo.taily.entity.MessageRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomExistsResponseDto {
    private Long roomId;
    private String user1Name;
    private String user2Name;

    public static ChatRoomExistsResponseDto from(MessageRoom room) {
        return new ChatRoomExistsResponseDto(
                room.getId(),
                room.getUser1().getNickname(),
                room.getUser2().getNickname()
        );
    }
}
