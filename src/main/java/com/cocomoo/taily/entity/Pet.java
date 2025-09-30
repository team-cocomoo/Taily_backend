package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "tableType"})
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 반려동물 이름
    @Column(nullable = false, length = 50)
    private String name;

    // 성별 (ENUM → EnumType.STRING 매핑)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetGender gender;

    // 성격/취향
    @Column(nullable = false, length = 255)
    private String preference;

    // 자기소개 (nullable 허용)
    @Column(length = 255)
    private String introduction;

    // 생성일 (자동 입력)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일 (자동 업데이트)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 연락처 (nullable 허용)
    @Column(length = 50)
    private String tel;

    // 유저 (FK: users_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    // 반려동물 타입 (예: 강아지, 고양이) (FK: table_types_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false)
    private TableType tableType;

    @PrePersist
    protected void setDefaultTableType() {
        if (this.tableType == null) {
            this.tableType = TableType.builder().id(2L).build();
        }
    }
}


