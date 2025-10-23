package com.cocomoo.taily.config;

import com.cocomoo.taily.security.jwt.JwtHandshakeInterceptor;
import com.cocomoo.taily.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtil jwtUtil;

    /**
     * 메시지 브로커 설정
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 prefix (ex. /topic/alarm/{userId})
        registry.enableSimpleBroker("/topic");  // 클라이언트 구독용 prefix
        // 클라이언트가 메시지를 보낼 때 사용하는 prefix (ex. /app/sendAlarm)
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 메시지 전송 prefix
    }

    /**
     * WebSocket 엔드포인트 설정
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat") // 프론트에서 연결할 엔드포인트
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .setAllowedOriginPatterns("*");

//                .withSockJS();  // SockJS 사용 (fallback), 로컬 테스트 시는 괜찮지만, 배포시 주석 해제

    }
}
