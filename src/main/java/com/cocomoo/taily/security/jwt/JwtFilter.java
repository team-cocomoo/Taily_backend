package com.cocomoo.taily.security.jwt;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.security.jwt.TokenBlacklistService;
import com.cocomoo.taily.security.user.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


/**
 JwtFilter - 모든 요청에 대한 JWT(인증 토큰)검증 필터

 JWT 인증 플로우 정리
 1. 클라이언트 -> 서버에 요청 헤드 : Authorization Bearer token에 요청
 배어러(Bearer) 토큰은 토큰을 소유한 사람에게 접근 권한을 부여하는 보안 토큰의 한 종류
 Bearer 사전적 의미 소유자, 즉 이 토큰을 소유한 소유자에게 접근 권한을 부여하라는 뜻
 2. JwtFilter 가 위처럼 전달된 jwt를 검증
 3. 유효한 토큰이면 : Spring Security Context에 인증 정보 저장
 4. Controller / Service 등에서 Security Context 에 저장된 인증 정보 사용
 5. 응답 후 SecurityContext 자동 제거( Stateless 유지 )
 */

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService; // 블랙리스트 서비스 주입

    /**
     * 필터 처리 메인 메서드
     * 모든 HTTP 요청마다 자동으로 실행됨
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 요청 URI 로깅 (디버깅용)
        log.debug("JWT 필터 실행: {}", request.getRequestURI());

        // 1. Authorization 헤더 추출
        String authorization = request.getHeader("Authorization");

        // 2. Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.debug("Authorization 헤더 없음 또는 Bearer 토큰이 아님");
            // 토큰이 없어도 다음 필터로 진행 (로그인 없이 접근 가능한 경로도 있음)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰이 있다면 Bearer 제거하고 순수 토큰 추출
        String token = authorization.substring(7);  // "Bearer " 이후 문자열
        log.debug("토큰 추출 완료");
        try {
            // 4. 블랙리스트 검사 (로그아웃된 토큰 차단)
            if (tokenBlacklistService.contains(token)) {
                log.warn("차단된 JWT 토큰 접근 시도: {}", token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 5. 토큰 유효성 전체 검증 (서명 + 만료 시간)
            if (!jwtUtil.validateToken(token)) {
                log.warn("유효하지 않은 토큰 감지");
                // 유효하지 않은 토큰 인증 정보를 생성하지 않고 다음 필터로
                filterChain.doFilter(request, response);
                return;
            }

            // 6. 토큰에서 사용자 정보 추출
            Long id = jwtUtil.getId(token);
            String publicId = jwtUtil.getPublicId(token);
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            // ROLE prefix 보장
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            log.info("JWT 인증 성공: username={}, role={}", username, role);

            // 7. User 엔티티 생성 (토큰 정보로 임시 생성)
            // 실제 DB 조회 없이 토큰 정보만으로 인증 객체 생성
            User user = User.builder()
                    .id(id)
                    .publicId(publicId)
                    .username(username)
                    .password("") // JWT 인증이므로 비밀번호 불필요
                    .build();


            // 8. CustomMemberDetails 생성
            CustomUserDetails userDetails = new CustomUserDetails(user);

            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));

            // 9. Spring Security 인증 토큰 생성
            // 이미 JWT로 인증되었으므로 credentials(비밀번호)는 null
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,                      // Principal (인증 주체)
                    null,                               // Credentials (이미 인증됨)
                    userDetails.getAuthorities()      // Authorities (권한)
            );

            // 10. SecurityContext에 인증 정보 저장
            // 이 정보는 Controller나 Service에서 사용 가능
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("JWT 인증 성공: {} SecurityContext에 인증 정보 저장 완료", username);
        } catch (ExpiredJwtException e) {
            // 만료된 토큰
            request.setAttribute("expired", "true");
            log.warn("JWT 토큰 만료: {}", e.getMessage());
        } catch (Exception e) {
            // 유효하지 않은 토큰
            request.setAttribute("invalid", "true");
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
        }
        // 10. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}