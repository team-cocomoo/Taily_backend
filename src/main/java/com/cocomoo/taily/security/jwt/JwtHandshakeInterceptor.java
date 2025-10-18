package com.cocomoo.taily.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {

        // JWT 토큰 추출
        String token = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            var httpRequest = servletRequest.getServletRequest();

            // 헤더에서 먼저 시도
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }else if (authHeader != null) {
                token = authHeader; // Bearer 없이 들어온 경우 그대로 사용
            }

            // 쿼리 파라미터에서도 시도
            if (token == null) {
                token = httpRequest.getParameter("token");
            }
        }

        if (token != null && jwtUtil.validateToken(token)) {
            log.info("WebSocket 연결 인증 성공: {}", jwtUtil.getUsername(token));
            attributes.put("user", jwtUtil.getUsername(token));
            return true;
        } else {
            log.warn("WebSocket 연결 실패: JWT 없음 또는 유효하지 않음");
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // 필요시 후처리
    }
}
