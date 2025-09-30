package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_datas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "messageRoom"}) // 순환 참조 방지
public class MessageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name= "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 외래키 : users_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // 외래키 : message_rooms_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_rooms_id", nullable = false)
    private MessageRoom messageRoom;

    // 메시지 내용 업데이트
    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
        this.content = newContent.trim();
    }
}
