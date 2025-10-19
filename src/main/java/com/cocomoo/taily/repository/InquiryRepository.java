package com.cocomoo.taily.repository;


import com.cocomoo.taily.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Optional<Inquiry> findByParentInquiryId(Long parentId);

    Page<Inquiry> findByTitleContainingOrContentContainingOrUserNicknameContaining(
            String title, String content, String nickname, Pageable pageable);

    Page<Inquiry> findByUserId(Long userId, Pageable pageable);

    Page<Inquiry> findByUserIdAndTitleContainingOrUserIdAndContentContaining(
            Long userId1, String titleKeyword,
            Long userId2, String contentKeyword,
            Pageable pageable);

    @Query("SELECT i FROM Inquiry i " +
            "LEFT JOIN FETCH i.user u " +
            "LEFT JOIN FETCH i.parentInquiry p " +
            "LEFT JOIN FETCH i.childInquiry c " +
            "WHERE i.id = :id")
    Optional<Inquiry> findByIdWithRelations(@Param("id") Long id);
}
