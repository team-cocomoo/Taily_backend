package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

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
    private LocalDateTime date;

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

//    @OneToMany(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "table_tybes_id", nullable = false)
//    private ;

    // 1
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

}
