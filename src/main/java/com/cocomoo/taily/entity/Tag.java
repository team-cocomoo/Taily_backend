package com.cocomoo.taily.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity // JPA 엔티티 선언
@Table(name = "tags")  // 테이블 명 지정 - 생략시 클래스명을 snake case 로 변환
@Getter
@NoArgsConstructor // JPA 요구사항 - 기본 생성자 필수
@AllArgsConstructor // 모든 필드를 받는 생성자
@Builder // 롬복을 이용한 빌더 패턴 적용 ,  객체 생성을 효율적으로
@ToString
public class Tag {
    @Id // Primary Key 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)// AUTO_INCREMENT 설정
    @Column(name="id")
    private Long id;

    @Column(name="name",nullable = false,length = 50)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
