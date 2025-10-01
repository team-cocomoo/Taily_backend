//package com.cocomoo.taily.security.jwt;
//
//import io.jsonwebtoken.ExpiredJwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.kosa.myproject.entity.Member;
//import org.kosa.myproject.entity.MemberRole;
//import org.kosa.myproject.security.user.CustomMemberDetails;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///**
//    JwtFilter - 모든 요청에 대한 JWT(인증 토큰)검증 필터
//
//    JWT 인증 플로우 정리
//    1. 클라이언트 -> 서버에 요청 헤드 : Authorization Bearer token에 요청
//        배어러(Bearer) 토큰은 토큰을 소유한 사람에게 접근 권한을 부여하는 보안 토큰의 한 종류
//        Bearer 사전적 의미 소유자, 즉 이 토큰을 소유한 소유자에게 접근 권한을 부여하라는 뜻
//    2. JwtFilter 가 위처럼 전달된 jwt를 검증
//    3. 유효한 토큰이면 : Spring Security Context에 인증 정보 저장
//    4. Controller / Service 등에서 Security Context 에 저장된 인증 정보 사용
//    5. 응답 후 SecurityContext 자동 제거( Stateless 유지 )
// */
//
//@Slf4j
//@RequiredArgsConstructor
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//
//    /**
//     * 필터 처리 메인 메서드
//     * 모든 HTTP 요청마다 자동으로 실행됨
//     */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // 요청 URI 로깅 (디버깅용)
//        log.debug("JWT 필터 실행: {}", request.getRequestURI());
//
//        // 1. Authorization 헤더 추출
//        String authorization = request.getHeader("Authorization");
//
//        // 2. Authorization 헤더 검증
//        if (authorization == null || !authorization.startsWith("Bearer ")) {
//            log.debug("Authorization 헤더 없음 또는 Bearer 토큰이 아님");
//            // 토큰이 없어도 다음 필터로 진행 (로그인 없이 접근 가능한 경로도 있음)
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 3. 토큰이 있다면 Bearer 제거하고 순수 토큰 추출
//        String token = authorization.substring(7);  // "Bearer " 이후 문자열
//        log.debug("토큰 추출 완료");
//        try {
//             // 4. 토큰 유효성 전체 검증 (서명 + 만료 시간)
//            if (!jwtUtil.validateToken(token)) {
//                log.warn("유효하지 않은 토큰 감지");
//                // 유효하지 않은 토큰 인증 정보를 생성하지 않고 다음 필터로
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // 5. 토큰에서 사용자 정보 추출
//            Long id = jwtUtil.getId(token);
//            String username = jwtUtil.getUsername(token);
//            String name = jwtUtil.getName(token);
//            String role = jwtUtil.getRole(token);
//
//            log.info("JWT 인증 성공: username={}, role={}", username, role);
//
//            // 6. Member 엔티티 생성 (토큰 정보로 임시 생성)
//            // 실제 DB 조회 없이 토큰 정보만으로 인증 객체 생성
//            Member member = Member.builder()
//                    .id(id)
//                    .username(username)
//                    .name(name)
//                    .role(MemberRole.valueOf(role))  // 문자열을 Enum으로 변환
//                    .password("")  // 비밀번호는 필요 없음 (이미 인증됨)
//                    .build();
//
//            // 7. CustomMemberDetails 생성
//            CustomMemberDetails memberDetails = new CustomMemberDetails(member);
//
//            // 8. Spring Security 인증 토큰 생성
//            // 이미 JWT로 인증되었으므로 credentials(비밀번호)는 null
//            Authentication authToken = new UsernamePasswordAuthenticationToken(
//                    memberDetails,                      // Principal (인증 주체)
//                    null,                               // Credentials (이미 인증됨)
//                    memberDetails.getAuthorities()      // Authorities (권한)
//            );
//
//            // 9. SecurityContext에 인증 정보 저장
//            // 이 정보는 Controller나 Service에서 사용 가능
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//
//            log.debug("JWT 인증 성공: {} SecurityContext에 인증 정보 저장 완료", username);
//        } catch (ExpiredJwtException e) {
//            // 만료된 토큰
//            request.setAttribute("expired", "true");
//            log.warn("JWT 토큰 만료: {}", e.getMessage());
//        } catch (Exception e) {
//            // 유효하지 않은 토큰
//            request.setAttribute("invalid", "true");
//            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
//        }
//        // 10. 다음 필터로 진행
//        filterChain.doFilter(request, response);
//    }
//}
//
