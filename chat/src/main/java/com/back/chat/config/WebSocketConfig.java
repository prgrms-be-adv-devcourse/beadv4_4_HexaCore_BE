package com.back.chat.config;

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

    // common 모듈에서.
    // private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /* @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws/chat")
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS()
                .setAllowedOriginPatterns("*"); // 운영 시 도메인 제한 권장
    }
     */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 발행 prefix
        registry.enableSimpleBroker("/topic");

        // 클라이언트 → 서버로 보내는 메시지 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }
}
