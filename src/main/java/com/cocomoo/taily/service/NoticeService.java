package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Notice;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지 등록 (관리자)
     */
    @Transactional
    public Notice createNotice(String title, String content, User user) {
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .user(user)
                .view(0L)
                .build();

        return noticeRepository.save(notice);
    }

    /**
     * 전체 공지 목록 조회
     */
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    /**
     * 공지 상세 조회 (조회수 증가)
     */
    @Transactional
    public Notice getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));

        notice.setView(notice.getView() + 1); // 조회수 증가
        return noticeRepository.save(notice);
    }

    /**
     * 공지 수정
     */
    @Transactional
    public Notice updateNotice(Long id, String title, String content) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 공지사항이 존재하지 않습니다."));
        notice = Notice.builder()
                .id(notice.getId())
                .title(title)
                .content(content)
                .user(notice.getUser())
                .view(notice.getView())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();

        return noticeRepository.save(notice);
    }

    /**
     * 공지 삭제
     */
    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}
