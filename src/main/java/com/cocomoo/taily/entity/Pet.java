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

    // 반려동물 나이
    @Column(nullable = false)
    private int age;

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

    /**
     * 작성자 설정
     * - 반려 동물 프로필 생성 시 작성자 지정
     */
    public void assignUser(User user) {
        this.user = user;
    }

    /**
     * 반려 동물 프로필 수정
     * - 이름, 성별, 취향, 소개만 수정 가능
     * - pet의 연락처는 주인 번호로 수정
     * - 작성자와 선택된 일자는 변경 불가
     * - 입력값 검증 포함
     */
    public void updateMyPetProfile(String name, PetGender gender, int age, String preference, String introduction) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }

        if (gender != null) {
            this.gender = gender;
        }

        if (age != 0) {
            this.age = age;
        }

        if (preference != null && !preference.trim().isEmpty()) {
            this.preference = preference.trim();
        }

        if (introduction != null && !introduction.trim().isEmpty()) {
            this.introduction = introduction.trim();
        }
    }

    /**
     * 작성자 확인
     * - 수정/삭제 권한 체크
     * - Service 레이어에서 활용
     */
    public boolean isUser(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }
}


