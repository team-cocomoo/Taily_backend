package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.chat.ChatRoomListResponseDto;
import com.cocomoo.taily.dto.chat.ChatRoomResponseDto;
import com.cocomoo.taily.dto.chat.MessageCreateRequestDto;
import com.cocomoo.taily.dto.chat.MessageDataResponseDto;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.MessageData;
import com.cocomoo.taily.entity.MessageRoom;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.MessageDataRepository;
import com.cocomoo.taily.repository.MessageRoomRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final MessageRoomRepository messageRoomRepository;
    private final MessageDataRepository messageDataRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<ChatRoomListResponseDto> getMyRoomsWithLastMessage(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        List<MessageRoom> rooms = messageRoomRepository.findByUsernameWithUsers(username);

        return rooms.stream()
                .map(room -> {
                    // 상대방 찾기
                    User otherUser = room.getUser1().equals(currentUser) ? room.getUser2() : room.getUser1();

                    // 상대방 프로필 이미지 조회
                    String otherProfileImage = imageRepository
                            .findTopByUsersId_IdAndTableTypeId_Id(otherUser.getId(), 1L)
                            .map(Image::getFilePath)
                            .orElse(null);

                    // 마지막 메시지 조회
                    MessageData lastMessage = messageDataRepository
                            .findTopByMessageRoomOrderByCreatedAtDesc(room)
                            .orElse(null);

                    return ChatRoomListResponseDto.from(room, currentUser, otherProfileImage, lastMessage);
                })
                .collect(Collectors.toList());
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomResponseDto createRoom(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new IllegalArgumentException("user1 없음"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new IllegalArgumentException("user2 없음"));

        // 이미 두 사용자가 같은 방을 가지고 있는지 검사
        MessageRoom existingRoom = messageRoomRepository.findByUsers(user1, user2).orElse(null);

        if (existingRoom != null) {
            String user1Profile = imageRepository.findTopByUsersId_IdAndTableTypeId_Id(existingRoom.getUser1().getId(), 1L)
                    .map(Image::getFilePath).orElse(null);
            String user2Profile = imageRepository.findTopByUsersId_IdAndTableTypeId_Id(existingRoom.getUser2().getId(), 1L)
                    .map(Image::getFilePath).orElse(null);
            return ChatRoomResponseDto.from(existingRoom, user1Profile, user2Profile);
        }

        MessageRoom room = messageRoomRepository.save(
                MessageRoom.builder()
                        .user1(user1)
                        .user2(user2)
                        .build()
        );
        // 새로 생성한 방의 프로필 이미지 조회
        String user1Profile = imageRepository.findTopByUsersId_IdAndTableTypeId_Id(user1.getId(), 1L)
                .map(Image::getFilePath).orElse(null);
        String user2Profile = imageRepository.findTopByUsersId_IdAndTableTypeId_Id(user2.getId(), 1L)
                .map(Image::getFilePath).orElse(null);

        log.info("채팅방 생성: id={}, user1={}, user2={}", room.getId(), user1.getUsername(), user2.getUsername());

        return ChatRoomResponseDto.from(room, user1Profile, user2Profile);
    }

    // 특정 채팅방의 이전 메시지 조회
    public List<MessageDataResponseDto> getMessagesByRoom(Long roomId, String username) {
        MessageRoom room = messageRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        if (!room.getUser1().getUsername().equals(username) &&
                !room.getUser2().getUsername().equals(username)) {
            throw new SecurityException("이 채팅방에 접근할 수 없습니다.");
        }

        List<MessageData> messages = messageDataRepository.findByMessageRoomOrderByCreatedAtAsc(room);
        return messages.stream()
                .map(MessageDataResponseDto::from)
                .collect(Collectors.toList());
    }

    // 메세지 전송 및 db 저장
    @Transactional
    public void saveAndBroadcastMessage(MessageCreateRequestDto dto, String senderUserName) {
        MessageRoom room = messageRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        User sender = userRepository.findByUsername(senderUserName)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        if (dto.getSenderId() != null && !dto.getSenderId().equals(sender.getId())) {
            throw new SecurityException("로그인한 사용자와 senderId가 일치하지 않습니다.");
        }

        MessageData message = messageDataRepository.save(
                MessageData.builder()
                        .messageRoom(room)
                        .user(sender)
                        .content(dto.getContent())
                        .build()
        );

        // 저장된 메시지 정보를 응답 DTO로 변환해서 전송
        messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getRoomId(),
                MessageDataResponseDto.from(message)
        );

        log.info("메시지 저장 및 전송 완료: roomId={}, sender={}, content={}",
                dto.getRoomId(), sender.getUsername(), dto.getContent());
    }
}
