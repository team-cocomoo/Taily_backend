package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.chat.*;
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
                            .findFirstByUserOrderByCreatedAtDesc(otherUser)
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
    public ChatRoomResponseDto createRoomByPublicId(String senderPublicId, String receiverPublicId) {
        User user1 = userRepository.findByPublicId(senderPublicId)
                .orElseThrow(() -> new IllegalArgumentException("user1 없음"));
        User user2 = userRepository.findByPublicId(receiverPublicId)
                .orElseThrow(() -> new IllegalArgumentException("user2 없음"));

        if (user1.getId().equals(user2.getId())) {
            throw new IllegalArgumentException("자기 자신과는 채팅방을 생성할 수 없습니다.");
        }

        // 기존 로직 그대로 재사용
        MessageRoom existingRoom = messageRoomRepository.findByUsers(user1, user2).orElse(null);
        if (existingRoom != null) {
            String user1Profile = imageRepository.findFirstByUserOrderByCreatedAtDesc(existingRoom.getUser1())
                    .map(Image::getFilePath).orElse(null);
            String user2Profile = imageRepository.findFirstByUserOrderByCreatedAtDesc(existingRoom.getUser2())
                    .map(Image::getFilePath).orElse(null);
            return ChatRoomResponseDto.from(existingRoom, user1Profile, user2Profile);
        }

        MessageRoom room = messageRoomRepository.save(
                MessageRoom.builder().user1(user1).user2(user2).build()
        );

        String user1Profile = imageRepository.findFirstByUserOrderByCreatedAtDesc(user1)
                .map(Image::getFilePath).orElse(null);
        String user2Profile = imageRepository.findFirstByUserOrderByCreatedAtDesc(user2)
                .map(Image::getFilePath).orElse(null);

        return ChatRoomResponseDto.from(room, user1Profile, user2Profile);
    }

    public ChatRoomExistsResponseDto getRoomExists(String senderPublicId, String receiverPublicId) {
        User user1 = userRepository.findByPublicId(senderPublicId).orElse(null);
        User user2 = userRepository.findByPublicId(receiverPublicId).orElse(null);

        if (user1 == null || user2 == null) return null;

        return messageRoomRepository.findByUsers(user1, user2)
                .map(ChatRoomExistsResponseDto::from)
                .orElse(null);
    }

    public MessageRoom getRoomByPublicId(String senderPublicId, String receiverPublicId) {
        User user1 = userRepository.findByPublicId(senderPublicId).orElseThrow();
        User user2 = userRepository.findByPublicId(receiverPublicId).orElseThrow();
        return messageRoomRepository.findByUsers(user1, user2).orElse(null);
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

    public List<UserChatSearchResponseDto> searchUsersByNickname(String nickname) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(nickname);

        return users.stream()
                .map(user -> {
                    String profileImage = imageRepository.findFirstByUserOrderByCreatedAtDesc(user)
                            .map(Image::getFilePath)
                            .orElse(null);
                    return UserChatSearchResponseDto.from(user, profileImage);
                })
                .collect(Collectors.toList());
    }
}
