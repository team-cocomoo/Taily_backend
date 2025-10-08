package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "walk_diaries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WalkDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather", nullable = false)
    private WalkDiaryWeather walkDiaryWeather;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "begin_time", nullable = false)
    private LocalTime beginTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion", nullable = false)
    private WalkDiaryEmotion walkDiaryEmotion;

    @CreationTimestamp
    @Column(name= "created_at", nullable = false, updatable = false)
    private  LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_types_id", nullable = false, foreignKey = @ForeignKey(name="fk_walk_diaries_table_type"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TableType tableType;

    // ============ 연관관계 메서드 ============

    /**
     * 작성자 설정
     * - 산책 일지 생성 시 작성자 지정
     */
    public void assignUser(User user) {
        this.user = user;
    }

    @PrePersist
    protected void setDefaultTableType() {
        if (this.tableType == null) {
            this.tableType = TableType.builder().id(4L).build();
        }
    }

    // 비즈니스 메서드

    /**
     * 산책 일지 수정
     * - 날씨, 시작 시간, 끝 시간, 반려동물 기분, 일지 내용만 수정 가능
     * - 작성자와 선택된 일자는 변경 불가
     * - 입력값 검증 포함
     */
    public void updateWalkDiary(WalkDiaryWeather walkDiaryWeather, LocalTime beginTime, LocalTime endTime, WalkDiaryEmotion walkDiaryEmotion, String content) {
        if (walkDiaryWeather != null) {
            this.walkDiaryWeather = walkDiaryWeather;
        }

        if (beginTime != null) {
            this.beginTime = beginTime;
        }

        if (endTime != null) {
            this.beginTime = beginTime;
        }

        // 시간 검증: 종료 시간이 시작 시간 이전이면 예외 처리
        if (this.beginTime != null && this.endTime != null && this.endTime.isBefore(this.beginTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이전일 수 없습니다.");
        }

        if (walkDiaryEmotion != null) {
            this.walkDiaryEmotion = walkDiaryEmotion;
        }

        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    /**
     * 작성자 확인
     * - 수정/삭제 권한 체크
     * - Service 레이어에서 활용
     *
     * @param user
     * @return
     */
    public boolean isUser(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }

}
