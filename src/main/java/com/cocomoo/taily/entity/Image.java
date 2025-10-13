package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String uuid;           // 이미지 고유 식별자

    @Column(name = "file_path", nullable = false, length = 300)
    private String filePath;       // 실제 저장 경로

    @Column(name = "file_size", nullable = false, length = 20)
    private String fileSize;       // 이미지 용량

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "posts_id")
    private Long postsId;          // 연관된 Feed ID (유저 사진에서 사용시 null)
    // 외래키 아님, 여러개 테이블과 연결되어 있다. 백엔드에서 직접 관련된 기능 id 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;      // 업로더 ID (글 작성에서 사용 시 null)


    @Column(name = "table_types_id", nullable = false)
    private Long tableTypesId;
    // 외래키, 관계된 기능의 id 값이 들어감(유저는 1, 펫은 2, 피드는 3, 산책 다이어리 4, 테일리 프렌드는 5, 산책 경로는 6, 이벤트는 7 사용)
    
    @PrePersist
    protected void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
