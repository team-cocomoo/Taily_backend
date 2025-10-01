//package com.cocomoo.taily.security.jwt;
//
//import io.jsonwebtoken.Jwts;
//import lombok.extern.slf4j.Slf4j;
//import org.kosa.myproject.entity.Member;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
///**
//    JwtUtil - Jwt 토큰 생성 및 검증 유틸리티
//    Jwt 구조 3가지로 구성
//    - Header : 토큰 타입과 알고리즘 정보
//    - Payload : 사용자 정보(Claims)
//    - Signature : 서명 ===> 위 변조 방지용 서명
// */
//@Component
//@Slf4j
//public class JwtUtil {
//
//    private final SecretKey secretKey;  // 서명용 비밀키
//
//    /**
//     * 생성자 - 비밀키 초기화
//     * application.properties의 설정값을 주입받아 SecretKey 생성
//     *
//     * @param secret Base64로 인코딩된 비밀키 문자열
//     */
//    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
//        // 문자열 비밀키를 SecretKey 객체로 변환
//        this.secretKey = new SecretKeySpec(
//                secret.getBytes(StandardCharsets.UTF_8),
//                Jwts.SIG.HS256.key().build().getAlgorithm()
//        );
//        log.info("JWT 비밀키 초기화 완료");
//    }
//
//    /**
//     * JWT 토큰 생성
//     *
//     * @param member 회원 정보
//     * @param expiredMs 만료 시간 (밀리초)
//     * @return 생성된 JWT 토큰 문자열
//     */
//    public String createJwt(Member member, Long expiredMs) {
//        log.info("JWT 토큰 생성 시작: username={}", member.getUsername());
//
//        // 현재 시간
//        Date now = new Date(System.currentTimeMillis());
//
//        // 만료 시간 계산
//        Date expiration = new Date(System.currentTimeMillis() + expiredMs);
//
//        // JWT 토큰 생성
//        String token = Jwts.builder()
//                // Payload (Claims) 설정
//                .claim("id", member.getId())              // 회원 번호 (PK)
//                .claim("username", member.getUsername())  // 로그인 ID
//                .claim("name", member.getName())          // 사용자 실명
//                .claim("role", member.getRole().name())   // 권한 (ROLE_USER, ROLE_ADMIN)
//
//                // 토큰 발급 시간
//                .issuedAt(now)
//
//                // 토큰 만료 시간
//                .expiration(expiration)
//
//                // 서명
//                .signWith(secretKey)
//
//                // 토큰 생성
//                .compact();
//
//        log.info("JWT 토큰 생성 완료: 만료시간={}ms", expiredMs);
//        return token;
//    }
//
//    /**
//     * 토큰에서 회원 ID(PK) 추출
//     *
//     * @param token JWT 토큰
//     * @return 회원 ID
//     */
//    public Long getId(String token) {
//        Long id = Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .get("id", Long.class);
//
//        log.debug("토큰에서 ID 추출: {}", id);
//        return id;
//    }
//
//    /**
//     * 토큰에서 username(로그인 ID) 추출
//     *
//     * @param token JWT 토큰
//     * @return username
//     */
//    public String getUsername(String token) {
//        String username = Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .get("username", String.class);
//
//        log.debug("토큰에서 username 추출: {}", username);
//        return username;
//    }
//
//    /**
//     * 토큰에서 사용자 실명 추출
//     *
//     * @param token JWT 토큰
//     * @return 사용자 실명
//     */
//    public String getName(String token) {
//        String name = Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .get("name", String.class);
//
//        log.debug("토큰에서 name 추출: {}", name);
//        return name;
//    }
//
//    /**
//     * 토큰에서 권한(Role) 추출
//     *
//     * @param token JWT 토큰
//     * @return 권한 (ROLE_USER, ROLE_ADMIN)
//     */
//    public String getRole(String token) {
//        String role = Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .get("role", String.class);
//
//        log.debug("토큰에서 role 추출: {}", role);
//        return role;
//    }
//
//    /**
//     * 토큰 만료 여부 검증
//     *
//     * @param token JWT 토큰
//     * @return true: 만료됨, false: 유효함
//     */
//    public Boolean isExpired(String token) {
//        try {
//            Date expiration = Jwts.parser()
//                    .verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload()
//                    .getExpiration();
//
//            boolean expired = expiration.before(new Date());
//
//            if (expired) {
//                log.warn("토큰이 만료됨: 만료시간={}", expiration);
//            }
//
//            return expired;
//        } catch (Exception e) {
//            log.error("토큰 만료 검증 실패: {}", e.getMessage());
//            return true;  // 검증 실패 시 만료된 것으로 처리
//        }
//    }
//
//    /**
//     * 토큰 유효성 전체 검증
//     * 서명 검증 + 만료 시간 체크
//     *
//     * @param token JWT 토큰
//     * @return true: 유효함, false: 유효하지 않음
//     */
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(token);
//
//            return !isExpired(token);
//        } catch (Exception e) {
//            log.error("토큰 검증 실패: {}", e.getMessage());
//            return false;
//        }
//    }
//}