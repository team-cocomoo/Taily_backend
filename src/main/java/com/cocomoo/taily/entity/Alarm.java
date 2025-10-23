package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Alarm 엔티티
 */
@Entity
@Table(name = "alarms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String content;

    @Column(nullable = false)   // 읽음 여부 (true : 읽음, false : 안읽음)
    private Boolean state = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;    // 생성 일시

    @Column(name = "posts_id", nullable = false)
    private Long postsId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmCategory category;

    // 연관 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;  // 알람 발생시킨 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false)
    private TableType tableType;

    // =================== 비즈니스 로직 ===================

    /**
     * 알람을 읽음 상태로 변경
     */
    public void markAsRead() {
        if (!this.state) {
            // 이미 읽은 알림이면 중복 호출 방지
            this.state = true;
        }
    }

    /**
     * 알람 내용을 수정 (관리자용, 혹은 재사용 시)
     */
    public void updateContent(String newContent) {
        if (newContent != null && !newContent.isBlank()) {
            this.content = newContent.trim();
        }
    }

    /**
     * 알람을 재전송하거나 상태를 초기화할 때 사용
     */
    public void resetState() {
        this.state = false;
    }

}
