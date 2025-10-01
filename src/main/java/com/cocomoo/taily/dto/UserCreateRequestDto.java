package com.cocomoo.taily.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 가입 요청 DTO
 * - 클라이언트 -> 서버로 전달되는 회원가입 정보
 * - Entity와 분리하여 계층 간 의존성 감소
 * - 검증 로직은 Service 레이어에서 처리
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequestDto {
    private String username; // 로그인 ID (Entity 필드명과 일치)

    private String publicId; // 외부로 노출되는 아이디

    private String password; // 비밀번호

    private String tel; // 전화번호

    private String email; // 이메일

    private String address; // 주소

    private String introduction; // 자기 소개
    
    private String role; // 회원 종류
    
    private String state; // 회원 상태
    
    private String createdAt; // 회원 생성일
    
    private String updatedAt; // 회원 정보 업데이트 날짜
    
    private String tableTypeId; // 테이블 아이디

}