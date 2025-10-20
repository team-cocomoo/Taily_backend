package com.cocomoo.taily.dto.chat;

import com.cocomoo.taily.dto.chat.MessageDataResponseDto;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.MessageData;
import com.cocomoo.taily.entity.MessageRoom;
import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 상세 응답 DTO
 * - 이전 메시지 목록
 * - 상대방 프로필, 닉네임 등
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDetailResponseDto {
    private Long roomId;
    private String otherUsername;
    private String otherNickname;
    private String otherProfileImage;
    private List<MessageDataResponseDto> messages;

    public static ChatRoomDetailResponseDto from(
            MessageRoom room,
            User currentUser,
            String otherProfileImage,
            List<MessageData> messageList
    ) {
        User otherUser = room.getUser1().equals(currentUser)
                ? room.getUser2()
                : room.getUser1();

        return ChatRoomDetailResponseDto.builder()
                .roomId(room.getId())
                .otherUsername(otherUser.getUsername())
                .otherNickname(otherUser.getNickname())
                .otherProfileImage(otherProfileImage)
                .messages(messageList.stream()
                        .map(MessageDataResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
