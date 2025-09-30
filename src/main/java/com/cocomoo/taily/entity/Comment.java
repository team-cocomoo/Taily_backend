package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Comment 엔티티
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "posts_id", nullable = false)
    private Long postsId;
    @Column(name = "parent_comment_id", nullable = false)
    private Long parentCommentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User usersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false)
    private TableType tableTypesId;
}
