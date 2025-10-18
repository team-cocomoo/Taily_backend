package com.cocomoo.taily.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * ✅ WebSocket 연결 및 STOMP CONNECT 시점 JWT 검증 모두 지원
 */
@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor, ChannelInterceptor {

    private final JwtUtil jwtUtil;

    /** ✅ 1️⃣ Handshake 단계 (SockJS 초기 연결) */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            var httpRequest = servletRequest.getServletRequest();
            String token = null;

            // 1. 헤더에서 토큰 추출
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // 2. 쿼리 파라미터에서 토큰 추출 (?token=...)
            if (token == null) {
                token = httpRequest.getParameter("token");
            }

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                attributes.put("user", username);
                log.info("✅ WebSocket Handshake 인증 성공: {}", username);
                return true;
            }
        }

        log.warn("❌ WebSocket Handshake 인증 실패");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }

    /** ✅ 2️⃣ STOMP CONNECT 단계 (connectHeaders.Authorization 검증) */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                accessor.setUser(() -> username);
                log.info("✅ STOMP CONNECT 인증 성공: {}", username);
            } else {
                log.warn("❌ STOMP CONNECT 인증 실패 (token: {})", token);
                throw new IllegalArgumentException("Invalid STOMP token");
            }
        }
        return message;
    }
}
