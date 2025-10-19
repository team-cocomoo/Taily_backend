package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.notice.NoticeRequestDto;
import com.cocomoo.taily.dto.notice.NoticeResponseDto;
import com.cocomoo.taily.entity.Notice;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.NoticeRepository;
import com.cocomoo.taily.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    /**
     * 공지 등록 (관리자)
     */
    @Transactional
    public NoticeResponseDto createNotice(NoticeRequestDto dto, String publicId) {
        User admin = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .user(admin)
                .view(0L)
                .build();

        Notice saved = noticeRepository.save(notice);
        log.info("공지 등록 완료: {}", saved.getId());
        return NoticeResponseDto.fromEntity(saved);
    }

    /**
     * 공지 목록 조회 (검색 + 페이지네이션)
     */
    public Page<NoticeResponseDto> getNotices(Pageable pageable, String keyword) {
        Page<Notice> noticePage;

        if (keyword != null && !keyword.isBlank()) {
            noticePage = noticeRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(keyword, pageable);
        } else {
            noticePage = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return noticePage.map(NoticeResponseDto::fromEntity);
    }

    /**
     * 공지 상세 조회 (조회수 증가)
     */
    @Transactional
    public NoticeResponseDto getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        notice.increaseView();
        return NoticeResponseDto.fromEntity(notice);
    }

    /**
     * 공지 수정 (관리자)
     */
    @Transactional
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto dto, String publicId) {
        User admin = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 공지사항이 존재하지 않습니다."));

        if (!notice.getUser().equals(admin)) {
            throw new SecurityException("본인이 작성한 공지만 수정할 수 있습니다.");
        }

        notice.update(dto.getTitle(), dto.getContent());
        return NoticeResponseDto.fromEntity(notice);
    }

    /**
     * 공지 삭제 (관리자)
     */
    @Transactional
    public void deleteNotice(Long id, String publicId) {
        User admin = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 공지사항이 존재하지 않습니다."));

        if (!notice.getUser().equals(admin)) {
            throw new SecurityException("본인이 작성한 공지만 삭제할 수 있습니다.");
        }

        noticeRepository.delete(notice);
    }
}
