package com.cocomoo.taily.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "taily_friends")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TailyFriend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(nullable = false, length = 200)
    private String address;

    @Builder.Default
    @Column(name = "view", nullable = false)
    private Long view = 0L;

    @Builder.Default
    @Column(name = "like", nullable = false)
    private Long likeCount = 0L;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false, foreignKey = @ForeignKey(name="fk_taily_friends_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false, foreignKey = @ForeignKey(name="fk_taily_friends_table_type"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TableType tableType;

    public void updatePost(String title, String content, String address) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        // 내용 검증 및 수정
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
        if(address != null && !address.trim().isEmpty()){
            this.address = address.trim();
        }
    }
    public boolean isAuthor(User user) {
        return this.user != null &&
                this.user.getId().equals(user.getId());
    }

    @PrePersist
    protected void setDefaultTableType() {
        if (this.tableType == null) {
            this.tableType = TableType.builder().id(5L).build();
        }
    }
}
