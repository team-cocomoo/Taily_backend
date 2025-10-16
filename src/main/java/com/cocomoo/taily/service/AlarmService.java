package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.alarm.AlarmResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.AlarmRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.TailyFriendRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final AlarmRepository alarmRepository;
    private final TailyFriendRepository tailyFriendRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendCommentAlarm(String username, Long postId, Long parentCommentId) {
        log.info("댓글 알람 발생 : username = {}, postId = {}, parentCommentId = {}", username, postId, parentCommentId);

        // 댓글 작성자 (sender) 조회
        User sender = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("댓글 작성자 정보를 가져올 수 없습니다."));

        // 게시글 작성자(알림 받을 사람) 결정
        User receiver;
        if (parentCommentId != null) {
            // 대댓글이면 부모 댓글 작성자
            Comment parentComment = tailyFriendRepository.getCommentById(parentCommentId);
            receiver = parentComment.getUsersId();
        } else {
            // 일반 댓글이면 게시글 작성자
            receiver = getPostWriterById(postId);
        }

        // 본인에게는 알람 생략
        if (receiver.getId().equals(sender.getId())) {
            log.info("본인 게시글에 댓글 작성 - 알람 전송 생략");
            return;
        }

        // TableType 조회
        TableType tableType = tableTypeRepository.findById(5L).orElseThrow(() -> new IllegalArgumentException("해당 TableType이 존재하지 않습니다."));

        // 알림 생성 및 저장
        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .content(sender.getUsername() + (parentCommentId != null ? "님이 회원님의 댓글에 답글을 남겼습니다." : "님이 회원님의 게시글에 댓글을 남겼습니다."))
                .postsId(postId)
                .state(false)
                .tableTypeId(tableType)
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);

        // WebSocket으로 전송
        AlarmResponseDto alarmDto = AlarmResponseDto.from(savedAlarm);
        messagingTemplate.convertAndSend("/topic/alarm/" + receiver.getId(), alarmDto);

        log.info("댓글 알람 전송 완료 → 수신자 ID: {}", receiver.getId());
    }

    /**
     * 게시글 작성자 조회 (알람 수신자)
     */
    private User getPostWriterById(Long postId) {
        TailyFriend post = tailyFriendRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return post.getUser();
    }
}
