package com.cocomoo.taily.security.jwt;

/**
 * JwtFilter - 모든 요청에 대한 JWT(인증 토큰)검증 필터
 *
 * JWT 인증 플로우 정리
 * 1. 클라이언트 -> 서버에 요청 헤드 : Authoriztion Bearer token에 요청
 *    배어러(Bearer) 토큰은 토큰을 소유한 사람에게 접근 권한을 부여하는 보안 토큰에 한 종류
 *    Bearer 사전적 의미 소유자, 즉 이 토큰을 소유한 소유자에게 접근 권한을 부여하라는 뜻
 * 2. JwtFilter가 위처럼 전달된 jwt를 검증
 * 3. 유효한 토큰이면 : Spring Security Context에 인증 정보 저장
 * 4. Controller / Service 등에서 Spring
 */

public class JwtFilter {
}
