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
public class MessageCreateRequestDto {
    private Long roomId;
    private Long senderId;
    private String content;

}
