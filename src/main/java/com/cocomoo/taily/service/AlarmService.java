package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.alarm.AlarmResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final AlarmRepository alarmRepository;
    private final FeedRepository feedRepository;
    private final TailyFriendRepository tailyFriendRepository;
    private final WalkPathRepository walkPathRepository;
    private final MessageRoomRepository messageRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 댓글 알람 (모든 게시판 공통)
     *
     * @param username
     * @param postId
     * @param parentCommentId
     */
    @Transactional
    public void sendCommentAlarm(String username, Long postId, Long parentCommentId, Long tableTypeId) {
        log.info("[AlarmService] 댓글 알람 발생 : username={}, postId={}, parentCommentId={}, tableTypeId={}",
                username, postId, parentCommentId, tableTypeId);

        // 댓글 작성자 (sender) 조회
        User sender = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("댓글 작성자 정보를 가져올 수 없습니다."));

        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        // 게시글 작성자(알림 받을 사람) 결정
        User receiver = getPostWriterByTableType(postId, tableType);

        if (parentCommentId != null) {
            // 대댓글이면 부모 댓글 작성자
            Comment parentComment = findParentCommentByTableType(tableType, parentCommentId);
            if (parentComment != null) {
                receiver = parentComment.getUsersId();
            }
        }

        // 본인에게는 알람 생략
        if (receiver.getId().equals(sender.getId())) {
            log.info("본인 게시글에 댓글 작성 - 알람 전송 생략");
            return;
        }

        // 알림 생성 및 저장
        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .content(
                        parentCommentId != null
                                ? "님이 회원님의 댓글에 답글을 남겼습니다."
                                : "님이 회원님의 게시글에 댓글을 남겼습니다."
                )
                .postsId(postId)
                .state(false)
                .tableType(tableType)
                .category(AlarmCategory.COMMENT)
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);

        // WebSocket으로 전송
        AlarmResponseDto alarmDto = AlarmResponseDto.from(savedAlarm);
        messagingTemplate.convertAndSend("/topic/alarm/" + receiver.getPublicId(), alarmDto);

        log.info("댓글 알람 전송 완료 → 수신자 ID: {}", receiver.getId());
    }

    /**
     * 게시판별 부모 댓글 찾기
     *
     * @param tableType
     * @param parentCommentId
     * @return
     */
    private Comment findParentCommentByTableType(TableType tableType, Long parentCommentId) {
        TableTypeCategory category = tableType.getCategory();

        return switch (category) {
            case TAILY_FRIENDS ->
                    tailyFriendRepository.getCommentById(parentCommentId);
            case FEEDS ->
                    feedRepository.getCommentById(parentCommentId);
            case WALK_PATHS ->
                    walkPathRepository.getCommentById(parentCommentId);
            default ->
                    null; // 혹은 throw new IllegalArgumentException("지원하지 않는 게시판 타입입니다: " + category);
        };
    }

    /**
     * 게시글 작성자 조회 (알람 수신자)
     */
    private User getPostWriterById(Long postId) {
        TailyFriend post = tailyFriendRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return post.getUser();
    }

    /**
     * 좋아요 알람
     *
     * @param username
     * @param postId
     */
    @Transactional
    public void sendLikeAlarm(String username, Long postId, Long tableTypeId) {
        log.info("[AlarmService] 좋아요 알람 발생 : username={}, postId={}", username, postId);

        // 좋아요 작성자 (sender) 조회
        User sender = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 TableType이 존재하지 않습니다."));

        // 게시판별 Repository에서 게시글 작성자 (receiver) 조회
        User receiver = getPostWriterByTableType(postId, tableType);

        // 본인 게시글이면 알람 생략
        if (receiver.getId().equals(sender.getId())) {
            log.info("[AlarmService] 본인 게시글 좋아요 - 알람 전송 생략");
            return;
        }

        // 알람 생성 및 저장
        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .content("님이 회원님의 게시글을 좋아합니다.")
                .postsId(postId)
                .state(false)
                .tableType(tableType)
                .category(AlarmCategory.LIKE)
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);

        // WebSocket으로 전송
        AlarmResponseDto alarmDto = AlarmResponseDto.from(savedAlarm);
        messagingTemplate.convertAndSend("/topic/alarm/" + receiver.getPublicId(), alarmDto);

        log.info("[AlarmService] 좋아요 알람 전송 완료 → 수신자 ID: {}", receiver.getId());

    }

    /**
     * 게시판별 작성자 조회 메서드
     *
     * @param postId
     * @param tableType
     * @return
     */
    private User getPostWriterByTableType(Long postId, TableType tableType) {
        TableTypeCategory tableTypeCategory = tableType.getCategory(); // Enum 자체로 받기

        return switch (tableTypeCategory) {
            case TAILY_FRIENDS ->
                    tailyFriendRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("테일리 프렌즈 게시글 없음"))
                            .getUser();
            case FEEDS ->
                    feedRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("피드 없음"))
                            .getUser();
            case WALK_PATHS ->
                    walkPathRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("산책 경로 없음"))
                            .getUser();
            default ->
                    throw new IllegalArgumentException("지원하지 않는 게시판 타입입니다. " + tableTypeCategory);
        };
    }

    /**
     * 팔로우 알람
     *
     * @param followerUsername
     * @param followingId
     */
    @Transactional
    public void sendFollowAlarm(String followerUsername, Long followingId) {
        log.info("[AlarmService] 팔로우 알람 발생: follower={}, followingId={}", followerUsername, followingId);

        // 팔로우한 사람 (보내는 사람)
        User sender = userRepository.findByUsername(followerUsername).orElseThrow(() -> new IllegalArgumentException("팔로우 요청한 사용자를 찾을 수 없습니다."));

        // 팔로우 받은 사람 (알람 수신자)
        User receiver = userRepository.findById(followingId).orElseThrow(() -> new IllegalArgumentException("팔로우 대상 사용자를 찾을 수 없습니다."));

        // 본인에게 팔로우한 경우 방지
        if (sender.getId().equals(receiver.getId())) {
            log.info("[AlarmService] 본인에게 팔로우 시도 - 알람 전송 생략");
            return;
        }

        log.info("🚀 WebSocket 알림 전송 시도 → receiverPublicId={}", receiver.getPublicId());


        // Users의 팔로우용 TableType 엔티티 가져오기
        TableType tableType = tableTypeRepository.findByCategory(TableTypeCategory.USERS).orElseThrow(() -> new IllegalArgumentException("TableTypeCategory.USERS에 해당하는 TableType이 없습니다."));

        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .content("님이 회원님을 팔로우했습니다.")
                .postsId(followingId)   // 게시글이 없으므로 follwngId로 대체
                .state(false)
                .tableType(tableType)
                .category(AlarmCategory.FOLLOW)
                .build();
        Alarm savedAlarm = alarmRepository.save(alarm);

        // 실시간 알람 전송
        AlarmResponseDto alarmResponseDto = AlarmResponseDto.from(savedAlarm);
        messagingTemplate.convertAndSend("/topic/alarm/" + receiver.getPublicId(), alarmResponseDto);

        log.info("[AlarmService] 팔로우 알람 전송 완료 → {}", receiver.getUsername());
    }

    // 채팅 알람
    public void sendChattingAlarm(String username, Long roomId) {
        log.info("[AlarmService] 채팅 알람 발생: username={}, roomId={}", username, roomId);

        // 채팅을 보내는 사람 (생성 시 처음 채팅을 시작하는 사람)
        User sender = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("채팅을 요청한 사람을 찾을 수 없습니다."));

        // 채팅창 정보 가져오기
        MessageRoom room = messageRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("해당 채팅방을 찾을 수 없습니다."));

        // 받는 사람 결정 (알람을 전송받는 사람, 보낸 사람은 제외)
        User receiver;
        if (room.getUser1().getId().equals(sender.getId())) {
            receiver = room.getUser2();
        } else {
            receiver = room.getUser1();
        }

        // 본인에게 보내는 경우 방지
        if (sender.getId().equals(receiver.getId())) {
            log.info("[AlarmService] 본인에게 채팅 시도 - 알람 전송 생략");
            return;
        }

        // Users의 팔로우용 TableType 엔티티 가져오기
        TableType tableType = tableTypeRepository.findByCategory(TableTypeCategory.USERS).orElseThrow(() -> new IllegalArgumentException("TableTypeCategory.USERS에 해당하는 TableType이 없습니다."));

        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .content("님이 회원에게 새 메시지를 보냈습니다.")
                .postsId(roomId)   // 게시글이 없으므로 roomId 대체
                .state(false)
                .tableType(tableType)
                .category(AlarmCategory.CHATTING)
                .build();
        Alarm savedAlarm = alarmRepository.save(alarm);

        // 실시간 알람 전송
        AlarmResponseDto alarmResponseDto = AlarmResponseDto.from(savedAlarm);
        messagingTemplate.convertAndSend("/topic/alarm/" + receiver.getPublicId(), alarmResponseDto);

        log.info("[AlarmService] 채팅 알람 전송 완료 → sender={}, receiver={}, roomId={}",
                sender.getUsername(), receiver.getUsername(), roomId);    }

    public List<AlarmResponseDto> getAlarms(String username) {
        log.info("[AlarmService] 알람 목록 조회 요청 - username={}", username);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<Alarm> alarms = alarmRepository.findByReceiverOrderByCreatedAtDesc(user);

        return alarms.stream()
                .map(AlarmResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 알람 읽음 처리
     * @param alarmId
     */
    @Transactional
    public void markAsRead(Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId).orElseThrow(() -> new IllegalArgumentException("해당 알람이 존재하지 않습니다."));

        alarm.markAsRead();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendCommentAlarmAsync(String username, Long postId, Long parentCommentId, Long tableTypeId) {
        try {
            sendCommentAlarm(username, postId, parentCommentId, tableTypeId);
        } catch (Exception e) {
            log.warn("[AlarmService] 비동기 알람 전송 실패 (무시됨): {}", e.getMessage());
        }
    }
}
