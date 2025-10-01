package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "view", nullable = false)
    private Long view = 0L;
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feeds_users_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feeds_table_types_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TableType tableType;

    @PrePersist
    protected void setDefaultTableType() {
        if (this.tableType == null) {
            this.tableType = TableType.builder().id(3L).build();
        }
    }
    private TableType tableTypesId;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TagList> tagLists = new ArrayList<>();
}
