package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "faqs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목 (최대 100자)
    @Column(nullable = false, length = 100)
    private String title;

    // 본문 (MEDIUMTEXT)
    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    // 생성일 (자동 입력)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일 (자동 업데이트)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 작성자 (users 테이블과 FK 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    /**
     * 작성자 설정
     * - faq 생성 시 작성자 지정
     */
    public void assignUser(User user) {
        this.user = user;
    }


    /**
     * faq 수정
     * - 제목, 내용만 수정 가능
     * - 작성일은 변경 불가
     * - 입력값 검증 포함
     */
    public void updateFaq(String title, String content) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }

        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    /**
     * 작성자 확인
     * - 수정/삭제 권한 체크
     * - Service 레이어에서 활용
     */
    public boolean isUser(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }
}

