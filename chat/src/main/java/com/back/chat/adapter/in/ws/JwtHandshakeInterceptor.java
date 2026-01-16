package com.back.chat.adapter.in.ws;

import com.back.security.jwt.JWTUtil;
import com.back.security.principal.AuthPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_PRINCIPAL = "AUTH_PRINCIPAL";

    private final JWTUtil jwtUtil;

    public JwtHandshakeInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            var claims = jwtUtil.validateAndGetClaims(token);

            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            AuthPrincipal authPrincipal = new AuthPrincipal(userId, role);

            attributes.put(ATTR_PRINCIPAL, authPrincipal);

            return true;
        } catch (Exception e) {
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
    }

    private String resolveToken(ServerHttpRequest request) {
        // 1. 헤더에서 가져오는 경우.
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. 쿼리 파라미터에서 가져오는 경우. (SocketJS 사용)
        if (request instanceof ServletServerHttpRequest servlet) {
            HttpServletRequest httpServletRequest = servlet.getServletRequest();
            String token = httpServletRequest.getParameter("token");
            if (StringUtils.hasText(token)) return token;
        }

        return null;
    }
}
