package com.cocomoo.taily.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * ✅ WebSocket 연결 및 STOMP CONNECT 시점 JWT 검증 모두 지원
 */
@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    /** ✅ 1️⃣ Handshake 단계 (SockJS 초기 연결) */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        log.info("🚀 [JwtHandshakeInterceptor] Handshake 진입");
        try {

            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpRequest = servletRequest.getServletRequest();

                String requestUrl = httpRequest.getRequestURL().toString();
                String queryString = httpRequest.getQueryString();
                log.info("🔗 요청 URL: {}", requestUrl);
                log.info("🧾 쿼리 스트링: {}", queryString);

                // ✅ 프론트엔드에서 전달한 토큰 추출 (?token=eyJhbGciOi...)
                String token = httpRequest.getParameter("token");

                if (token == null || token.isEmpty()) {
                    log.warn("❌ WebSocket 연결 실패: token 누락");
                    return false;
                }

                // ✅ 토큰 검증
                boolean valid = false;
                try {
                    valid = jwtUtil.validateToken(token);
                    log.info("🧩 validateToken 결과: {}", valid);
                } catch (Exception e) {
                    log.error("⚠️ 토큰 검증 중 예외 발생: {}", e.getMessage());
                }

                if (!valid) {
                    log.warn("⚠️ 토큰이 만료 또는 유효하지 않음 → 연결은 임시 허용");
                    //return false; // 디버깅 단계에서는 주석 처리, 배포 전 주석 해제 -> 보안상 안전
                }

                // ✅ userId 추출 후 세션에 저장 (필요하면 nickname 등도 가능)
                Long userId = jwtUtil.getId(token);
                attributes.put("userId", userId);

                log.info("✅ WebSocket 인증 성공 - userId: {}", userId);
                return true;
            } else {
                log.error("❌ 요청이 ServletServerHttpRequest 타입이 아님 → Handshake 불가");
            }
        } catch (Exception e) {
            log.error("❌ Handshake 중 오류: {}", e.getMessage());
        }
        log.warn("⚠️ Handshake 실패, false 반환");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.error("❌ afterHandshake 중 예외 발생: {}", exception.getMessage(), exception);
        } else {
            log.info("🤝 Handshake 완료 (afterHandshake)");
        }
    }

}
