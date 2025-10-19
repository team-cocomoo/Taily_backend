package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 제목 검색 (최신순)
    Page<Notice> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    // 전체 목록 (최신순)
    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
