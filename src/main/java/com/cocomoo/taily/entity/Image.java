package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Image 엔티티
 */
@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String uuid;
    @Column(name = "file_path", nullable = false, length = 50)
    private String filePath;
    @Column(name = "fileSize", nullable = false, length = 50)
    private String fileSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "walk_diaries_id", nullable = false)
    private Long walkDiariesId;
    @Column(name = "post_id", nullable = false)
    private Long postsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false)
    private TableType tableTypeId;

}
