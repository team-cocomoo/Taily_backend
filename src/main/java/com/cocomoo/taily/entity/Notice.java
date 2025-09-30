package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity // JPA 엔티티 선언
@Table(name = "notices")  // 테이블 명 지정 - 생략시 클래스명을 snake case 로 변환
@Getter
@NoArgsConstructor // JPA 요구사항 - 기본 생성자 필수
@AllArgsConstructor // 모든 필드를 받는 생성자
@Builder // 롬복을 이용한 빌더 패턴 적용 ,  객체 생성을 효율적으로
@ToString
public class Notice {
    @Id // Primary Key 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)// AUTO_INCREMENT 설정
    @Column(name="id")
    private Long id;

    @Column(name="title",nullable = false,length = 50)
    private String title;

    @Lob
    @Column(name= "content", nullable = false)
    private String content;

    //조회수
    @Column(name = "view", nullable = false)
    private  Long view;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    // 외래키 : users_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;



}
