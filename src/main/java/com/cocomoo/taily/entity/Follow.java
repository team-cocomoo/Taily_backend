package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"following", "follower"})
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상태 ENUM
    @Enumerated(EnumType.STRING) // DB ENUM ↔ 자바 Enum
    @Column(nullable = false)
    private FollowState state;

    // 생성일
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 팔로잉하는 사람 (내가 팔로우한 대상)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    // 팔로워 (나를 팔로우한 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    public void deactivate() {
        this.state = FollowState.INACTIVE;
    }

    public void activate() {
        this.state = FollowState.ACTIVE;
    }

}

