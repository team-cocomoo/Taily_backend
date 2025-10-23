package com.cocomoo.taily.dto.chat;

import com.cocomoo.taily.entity.MessageData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDataResponseDto {
    private Long id;
    private String senderName;
    private String senderNickName;
    private String content;
    private LocalDateTime createdAt;

    public static MessageDataResponseDto from(MessageData messageData) {
        return MessageDataResponseDto.builder()
                .id(messageData.getId())
                .senderName(messageData.getUser().getUsername())
                .senderNickName(messageData.getUser().getNickname())
                .content(messageData.getContent())
                .createdAt(messageData.getCreatedAt())
                .build();
    }
}
