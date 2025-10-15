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

    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;
    // id

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

    @Column(name = "sanction_count", nullable = false)
    @Builder.Default
    private Long sanctionCount = 0L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER;
    // (추후 구현)관리자 유저 객체 생성시 ROLE_ADMIN 되게 처리해야됨

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

    @Column(name = "penalty_start_date")
    private LocalDateTime penaltyStartDate;

    @Column(name = "penalty_end_date")
    private LocalDateTime penaltyEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_table_type"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TableType tableType;

    @PrePersist
    protected void setDefaultTableType() {
        if (this.tableType == null) {
            this.tableType = TableType.builder().id(1L).build();
        }
    }

    /**
     * 회원 정보 수정
     * (비밀번호는 서비스 계층에서 반드시 암호화 후 전달해야 함)
     */
    public void updateInfo(String username,
                           String nickname,
                           String password,
                           String tel,
                           String email,
                           String address,
                           String introduction,
                           UserState state
    ) {
        if (username != null && !username.isBlank()) {
            this.username = username.trim();
        }
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname.trim();
        }
        if (password != null && !password.isBlank()) {
            this.password = password.trim();
        }
        if (tel != null && !tel.isBlank()) {
            this.tel = tel.trim();
        }
        if (email != null && !email.isBlank()) {
            this.email = email.trim();
        }
        if (address != null && !address.isBlank()) {
            this.address = address.trim();
        }
        if (introduction != null) { // 소개는 공백 가능
            this.introduction = introduction.trim();
        }
        if (state != null) {
            this.state = state;
        }
    }

    // 사용자 제재 메서드
    public void applyPenalty(int days) {
        if (days <= 0) {
            return;
        }

        this.state = UserState.SUSPENDED;
        this.penaltyStartDate = LocalDateTime.now();
        this.penaltyEndDate = this.penaltyStartDate.plusDays(days);
        this.sanctionCount += 1;
    }

    // 제재 해제 메서드
    public void liftPenalty() {
        this.state = UserState.ACTIVE;
        this.penaltyStartDate = null;
        this.penaltyEndDate = null;
    }

}