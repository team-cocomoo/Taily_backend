package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.LikeRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final LikeRepository likeRepository;
    private final AlarmService alarmService;

    @Transactional
    public boolean toggleLike(Long postId, String username, Long tableTypeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        Like like = likeRepository.findByPostsIdAndTableTypeAndUser(postId, tableType, user)
                .orElse(null);

        boolean isLiked;
        if (like == null) {
            log.info("[LikeService] 좋아요 클릭됨 - 알람 발송 시도: user={}, postId={}", username, postId);
            likeRepository.save(Like.builder()
                    .postsId(postId)
                    .user(user)
                    .tableType(tableType)
                    .state(true)
                    .build());
            isLiked = true;

            // 좋아요 클릭 시 알람 발송
            alarmService.sendLikeAlarm(username, postId, tableTypeId);
            log.info("[LikeService] sendLikeAlarm 호출 완료");

        } else {
            like.toggle();
            isLiked = like.isState();
            // 좋아요 취소 시에는 알람 생략
            log.info("[LikeService] 좋아요 토글 완료: user={}, postId={}, state={}", username, postId, isLiked);

        }
        return isLiked;
    }

    public Long getLikeCount(Long postId, Long tableTypeId) {
        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));
        return likeRepository.countByPostsIdAndTableTypeAndState(postId, tableType, true);
    }

    public boolean isLiked(Long postId, String username, Long tableTypeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));
        return likeRepository.findByPostsIdAndTableTypeAndUser(postId, tableType, user)
                .map(Like::isState)
                .orElse(false);
    }
}
