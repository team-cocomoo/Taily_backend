package com.cocomoo.taily.dto.chat;

import com.cocomoo.taily.entity.MessageData;
import com.cocomoo.taily.entity.MessageRoom;
import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListResponseDto {
    private Long roomId;
    private String otherUsername;
    private String otherUserNickname;
    private String otherProfileImage;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;

    public static ChatRoomListResponseDto from(MessageRoom room,
                                               User currentUser,
                                               String otherProfileImage,
                                               MessageData lastMessage) {
        User otherUser = room.getUser1().equals(currentUser) ? room.getUser2() : room.getUser1();
        return ChatRoomListResponseDto.builder()
                .roomId(room.getId())
                .otherUsername(otherUser.getUsername())
                .otherUserNickname(otherUser.getNickname())
                .otherProfileImage(otherProfileImage)
                .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .build();
    }
}
