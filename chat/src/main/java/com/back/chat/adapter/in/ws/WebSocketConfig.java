package com.back.chat.adapter.in.ws;

import com.back.security.jwt.JWTUtil;
import com.back.security.principal.AuthPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String WS_EXP_EPOCH_SEC = "ws:expEpochSec";
    private static final long EXP_SKEW_SEC = 5L; // clock skew buffer

    private final JWTUtil jwtUtil;

    public WebSocketConfig(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .addInterceptors(new WsHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (acc == null || acc.getCommand() == null) return message;

                StompCommand cmd = acc.getCommand();

                if (StompCommand.CONNECT.equals(cmd)) {
                    // 1) CONNECT: 토큰 검증(만료 포함) + principal 세팅 + exp 세션 저장
                    String auth = firstAuthHeader(acc);
                    if (auth == null || !auth.startsWith("Bearer ")) {
                        throw new MessagingException("UNAUTHORIZED");
                    }

                    String token = auth.substring(7);

                    final Claims claims;
                    try {
                        claims = jwtUtil.validateAndGetClaims(token);
                    } catch (ExpiredJwtException e) {
                        throw new MessagingException("TOKEN_EXPIRED", e);
                    } catch (JwtException e) {
                        throw new MessagingException("INVALID_TOKEN", e);
                    }

                    // userId(subject)
                    Long userId = parseUserId(claims.getSubject());

                    // role
                    String normalizedRole = normalizeRole(claims.get("role", String.class));

                    // exp 저장 (세션)
                    Date exp = claims.getExpiration();
                    if (exp == null) {
                        throw new MessagingException("INVALID_TOKEN");
                    }
                    long expEpochSec = exp.toInstant().getEpochSecond();

                    Map<String, Object> sessionAttrs = ensureSessionAttributes(acc);
                    sessionAttrs.put(WS_EXP_EPOCH_SEC, expEpochSec);

                    // principal 세팅
                    AuthPrincipal principal = new AuthPrincipal(userId, normalizedRole);
                    var authorities = List.of(new SimpleGrantedAuthority(normalizedRole));
                    var authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    acc.setUser(authentication);
                    return message;
                }

                // 2) CONNECT 이후: SEND, SUBSCRIBE 프레임에서 exp로 만료 체크 + user 존재 체크
                if (StompCommand.SEND.equals(cmd) || StompCommand.SUBSCRIBE.equals(cmd)) {
                    if (acc.getUser() == null) {
                        throw new MessagingException("UNAUTHORIZED");
                    }

                    Long expEpochSec = getSessionExp(acc);
                    if (expEpochSec == null) {
                        // exp 없으면 만료 추적 불가 -> 차단
                        throw new MessagingException("UNAUTHORIZED");
                    }

                    long now = Instant.now().getEpochSecond();
                    if (now >= (expEpochSec - EXP_SKEW_SEC)) {
                        throw new MessagingException("TOKEN_EXPIRED");
                    }
                }

                return message;
            }

            private Map<String, Object> ensureSessionAttributes(StompHeaderAccessor acc) {
                Map<String, Object> attrs = acc.getSessionAttributes();
                if (attrs != null) return attrs;

                // HandshakeInterceptor로 보장되지만, 혹시나를 대비해 생성/보정
                Map<String, Object> created = new HashMap<>();
                acc.setSessionAttributes(created);
                return created;
            }

            private String firstAuthHeader(StompHeaderAccessor acc) {
                // 1순위 Authorization
                String v = acc.getFirstNativeHeader("Authorization");
                if (v != null && !v.isBlank()) return v;

                // 2순위 커스텀 헤더(필요시 프론트에서 사용)
                v = acc.getFirstNativeHeader("X-Authorization");
                if (v != null && !v.isBlank()) return v;

                return null;
            }

            private Long getSessionExp(StompHeaderAccessor acc) {
                Map<String, Object> attrs = acc.getSessionAttributes();
                if (attrs == null) return null;

                Object v = attrs.get(WS_EXP_EPOCH_SEC);
                if (v instanceof Long l) return l;

                // 혹시 String으로 들어갔을 가능성 방어
                if (v instanceof String s) {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException ignore) {
                        return null;
                    }
                }
                return null;
            }

            private Long parseUserId(String subject) {
                if (subject == null || subject.isBlank()) {
                    throw new MessagingException("INVALID_TOKEN");
                }
                try {
                    return Long.parseLong(subject);
                } catch (NumberFormatException e) {
                    throw new MessagingException("INVALID_TOKEN");
                }
            }

            private String normalizeRole(String role) {
                if (role == null || role.isBlank()) {
                    throw new MessagingException("INVALID_TOKEN");
                }
                return role.startsWith("ROLE_") ? role : "ROLE_" + role;
            }
        });
    }

    /**
     * Handshake 단계에서 세션 attributes를 항상 준비해둔다.
     * SockJS 환경에서도 attrs Map은 제공되므로 여기서 보장해두면
     * CONNECT 시점에 acc.getSessionAttributes()가 null로 떨어질 확률이 크게 줄어든다.
     */
    public static class WsHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes
        ) {
            // attributes는 null이 아니지만, 혹시 모를 구현체 방어
            if (attributes == null) return true;

            // 여기서 뭔가를 꼭 넣을 필요는 없지만,
            // "빈 attrs라도 존재하게" 만드는 효과를 위해 더미 키를 넣어도 됨.
            // (원치 않으면 제거해도 됨)
            attributes.putIfAbsent("ws:handshake", Boolean.TRUE);

            return true;
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Exception exception
        ) {
            // no-op
        }
    }
}
