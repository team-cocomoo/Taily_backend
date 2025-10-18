package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.chat.*;
import com.cocomoo.taily.entity.MessageRoom;
import com.cocomoo.taily.service.AlarmService;
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
    private final AlarmService alarmService;
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
    public ResponseEntity<?> createRoomByPublicId(@RequestParam String senderPublicId,
                                                  @RequestParam String receiverPublicId) {
        ChatRoomResponseDto room = chatService.createRoomByPublicId(senderPublicId, receiverPublicId);
        log.info("채팅방 생성: {} - {}", room.getUser1Name(), room.getUser2Name());
        return ResponseEntity.ok(ApiResponseDto.success(room, "채팅방 생성 성공"));
    }

    // 채팅방 존재 여부 확인
    @GetMapping("/exists")
    public ResponseEntity<?> existsRoom(@RequestParam String senderPublicId,
                                        @RequestParam String receiverPublicId) {
        ChatRoomExistsResponseDto dto = chatService.getRoomExists(senderPublicId, receiverPublicId);
        if (dto != null) {
            return ResponseEntity.ok(ApiResponseDto.success(dto, "채팅방 존재 여부 확인"));
        } else {
            return ResponseEntity.ok(ApiResponseDto.success(null, "채팅방 존재하지 않음"));
        }
    }

    // 메시지 전송
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageCreateRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        chatService.saveAndBroadcastMessage(dto, username);
        log.info("메시지 전송: roomId={}, senderId={}", dto.getRoomId(), dto.getSenderId());

        // 메시지 전송 시 알람 위임
        alarmService.sendChattingAlarm(username, dto.getRoomId());

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
    // 채팅 전송(웹소켓)
    @MessageMapping("/chat.send")
    public void sendWebSocket(MessageCreateRequestDto messageCreateRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        chatService.saveAndBroadcastMessage(messageCreateRequestDto, username);
        log.info("WebSocket 메시지 전송: {}", messageCreateRequestDto.getContent());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String nickname) {
        List<UserChatSearchResponseDto> users = chatService.searchUsersByNickname(nickname);
        return ResponseEntity.ok(ApiResponseDto.success(users, "검색 결과"));
    }
}
