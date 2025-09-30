package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="public_id", nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String tel;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String introduction;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserState state = UserState.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false, foreignKey = @ForeignKey(name="fk_users_table_type"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TableType tableType;

    @PrePersist
    protected void setDefaultTableType() {
        if (this.tableType == null) {
            this.tableType = TableType.builder().id(1L).build();
        }
    }

}