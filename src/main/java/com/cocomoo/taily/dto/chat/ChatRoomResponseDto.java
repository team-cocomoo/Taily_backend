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
public class ChatRoomResponseDto {
    private Long roomId;
    private String user1Name;
    private String user2Name;
    private String user1ProfileImage;
    private String user2ProfileImage;

    public static ChatRoomResponseDto from(MessageRoom room,
                                           String user1Profile,
                                           String user2Profile) {
        return ChatRoomResponseDto.builder()
                .roomId(room.getId())
                .user1Name(room.getUser1().getUsername())
                .user2Name(room.getUser2().getUsername())
                .user1ProfileImage(user1Profile)
                .user2ProfileImage(user2Profile)
                .build();
    }
}