//package com.cocomoo.taily.security.jwt;
//
//import com.cocomoo.taily.dto.ApiResponseDto;
//import com.cocomoo.taily.dto.LoginRequestDto;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
///**
//    JSON 형식의 로그인 요청을 처리하는 필터
//    /api/auth/login 의 엔드 포인트로 오는 로그인 요청 처리
//    기존 FORM 로그인 대신 JSON 본문을 파싱해서 처리
// */
//@Slf4j
//public class JsonLoginFilter extends UsernamePasswordAuthenticationFilter {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;
//    private final ObjectMapper objectMapper = new ObjectMapper()
//            .registerModule(new JavaTimeModule())  // LocalDateTime 처리를 위한 모듈 등록
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO-8601 형식으로 출력
//;
//
//    public JsonLoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
//        this.authenticationManager = authenticationManager;
//        this.jwtUtil = jwtUtil;
//        // 로그인 엔드포인트 설정
//        setFilterProcessesUrl("/api/auth/login");
//    }
//
//    /**
//     *
//     *  로그인 시도 처리
//     *   POST /login 요청이 들어올 때 자동 실행
//     * JSON 본문에서 로그인 정보 추출
//     *
//     * @param request  HTTP 요청 (username, password 포함)
//     * @param response HTTP 응답
//     * @return Authentication 인증 결과
//     */
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request,
//                                              HttpServletResponse response)
//                                              throws AuthenticationException {
//
//        log.info("=== JSON 로그인 시도 ===");
//
//        try {
//            // JSON 본문을 LoginRequestDto로 변환
//            LoginRequestDto loginRequest = objectMapper.readValue(
//                request.getInputStream(),
//                LoginRequestDto.class
//            );
//
//            log.info("로그인 시도: username={}", loginRequest.getUsername());
//
//            // 인증 토큰 생성
//            UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(
//                    loginRequest.getUsername(),
//                    loginRequest.getPassword()
//                );
//
//            // 인증 시도
//            // AuthenticationManager에 인증 위임
//            // 내부적으로 다음 과정 실행:
//            // - AuthenticationProvider 호출
//            // - CustomMemberDetailsService.loadUserByUsername() 호출
//            // - DB에서 사용자 조회
//            // - 비밀번호 검증
//            log.info("AuthenticationManager에 인증 위임 ");
//            log.info("CustomMemberDetailsService.loadUserByUsername() 호출");
//            log.info("DB에서 사용자 조회, username(id)와 password 검증");
//            return authenticationManager.authenticate(authToken);
//
//        } catch (IOException e) {
//            log.error("로그인 요청 파싱 실패", e);
//            throw new RuntimeException("Invalid login request format");
//        }
//    }
//
//    /**
//     * 로그인 성공 시 JWT 토큰 생성 및 응답
//     */
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request,
//                                           HttpServletResponse response,
//                                           FilterChain chain,
//                                           Authentication authentication)
//                                           throws IOException, ServletException {
//
//        log.info("=== 로그인 성공: {} ===", authentication.getName());
//
//        // 1. 인증된 사용자 정보 추출
//        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
//        Member member = memberDetails.getMember();
//
//        // 2. 사용자 권한 추출
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//        GrantedAuthority auth = iterator.next();
//        String role = auth.getAuthority();  // ROLE_USER 또는 ROLE_ADMIN
//
//        log.info("인증 성공: username={}, role={}", member.getUsername(), role);
//
//        // 3. JWT 토큰 생성
//        // 하루로 유효 기간을 준다
//        long expiredMs = 1000L * 60 * 60 * 24; // 하루
//        String token = jwtUtil.createJwt(member, expiredMs);
//
//        // 4. 응답 헤더에 토큰 추가
//        // Bearer 스키마 사용 (JWT 표준)
//        response.addHeader("Authorization", "Bearer " + token);
//
//        // CORS를 위해 Authorization 헤더 노출 설정
//        response.addHeader("Access-Control-Expose-Headers", "Authorization");
//        //응답 헤더에 인코딩 및 status 추가
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        // 5. 응답 바디에 사용자 정보 추가 (JSON)
//        // 응답 데이터 생성
//        Map<String, Object> responseData = Map.of(
//                "id", member.getId(),
//                "username", member.getUsername(),
//                "name", member.getName(),
//                "role", role
//        );
//
//// ApiResponseDto.success()를 사용하여 표준 응답 생성
//        ApiResponseDto<Map<String, Object>> successResponse = ApiResponseDto.success(
//                responseData,
//                "로그인 성공"
//        );
//
//        // JSON 응답 전송
//        response.getWriter().write(objectMapper.writeValueAsString(successResponse));
//
//        log.info("JWT 토큰 발급 완료: username={}", member.getUsername());
//    }
//
//    /**
//     * 로그인 실패 시 에러 응답
//     */
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request,
//                                             HttpServletResponse response,
//                                             AuthenticationException failed)
//                                             throws IOException, ServletException {
//
//        log.error("=== 로그인 실패: {} ===", failed.getMessage());
//
//        // 에러 응답 설정
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("success", false);
//        errorResponse.put("message", "로그인 실패: 아이디 또는 비밀번호를 확인해주세요");
//        errorResponse.put("code", "AUTHENTICATION_FAILED");
//
//        // JSON 에러 응답 전송
//        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
//    }
//}