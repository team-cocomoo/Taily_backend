package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "walk_diaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalkDiaryWeather walkDiaryWeather;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "begin_time", nullable = false)
    private LocalTime beginTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name= "crated_at", nullable = false)
    private  LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

//    @OneToMany(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "table_tybes_id", nullable = false)
//    private ;

    // 1


}
