package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "walk_paths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WalkPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "view", nullable = false)
    private Long view = 0L;

    @Column(name = "like", nullable = false)
    private Long like = 0L;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false, foreignKey = @ForeignKey(name="fk_walk_paths_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false, foreignKey = @ForeignKey(name="fk_walk_paths_table_type"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TableType tableType;

    // 글 수정
    public void updatePost(String title, String content) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        // 내용 검증 및 수정
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    // 작성자인지 확인
    public boolean isAuthor(User user) {
        return this.user.getId().equals(user.getId());
    }
}
