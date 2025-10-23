package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Long view = 0L;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;
    // 외래키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false)
    private TableType tableType;
    // 외래키

    @Builder.Default
    // Feed ↔ Image 1:N
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Image> images = new ArrayList<>();
    // 관련된 이미지 리스트를 가져와서 사용, 일대다 관계

    @Builder.Default
    // Feed ↔ TagList 1:N
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TagList> tagLists = new ArrayList<>();
    // 관련된 태그 리스트를 가져와서 사용, 일대다 관계

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.tableType == null) this.tableType = TableType.builder().id(3L).build();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
