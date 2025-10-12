package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.chat.ChatRoomListResponseDto;
import com.cocomoo.taily.dto.chat.ChatRoomResponseDto;
import com.cocomoo.taily.dto.chat.MessageCreateRequestDto;
import com.cocomoo.taily.dto.chat.MessageDataResponseDto;
import com.cocomoo.taily.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatService chatService;

    // 내가 참여한 채팅방 조회
    @GetMapping
    public ResponseEntity<?> getMyRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<ChatRoomListResponseDto> rooms = chatService.getMyRoomsWithLastMessage(username);
        int count = rooms.size();
        return ResponseEntity.ok(ApiResponseDto.success(Map.of(
                "count", count,
                "rooms", rooms
        ), "참여한 채팅방 조회 성공"));
    }

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        ChatRoomResponseDto room = chatService.createRoom(user1Id, user2Id);
        log.info("채팅방 생성: {} - {}", room.getUser1Name(), room.getUser2Name());
        return ResponseEntity.ok(ApiResponseDto.success(room, "채팅방 생성 성공"));
    }

    // 메시지 전송
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageCreateRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        chatService.saveAndBroadcastMessage(dto, username);
        log.info("메시지 전송: roomId={}, senderId={}", dto.getRoomId(), dto.getSenderId());
        return ResponseEntity.ok(ApiResponseDto.success(null, "메시지 전송 성공"));
    }

    // 채팅방 채팅 조회
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(@PathVariable Long roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<MessageDataResponseDto> messages = chatService.getMessagesByRoom(roomId,username);
        return ResponseEntity.ok(ApiResponseDto.success(messages, "이전 메시지 조회 성공"));
    }

    @MessageMapping("/chat.send")
    public void sendWebSocket(MessageCreateRequestDto messageCreateRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        chatService.saveAndBroadcastMessage(messageCreateRequestDto, username);
        log.info("WebSocket 메시지 전송: {}", messageCreateRequestDto.getContent());
    }
}
