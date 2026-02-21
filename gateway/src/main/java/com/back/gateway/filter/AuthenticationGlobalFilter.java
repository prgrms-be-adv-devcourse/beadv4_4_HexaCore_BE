package com.back.gateway.filter;

import com.back.gateway.config.GatewayServiceProperties;
import com.back.security.header.GatewayHeaders;
import com.back.security.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

/**
 * 모든 요청에 대해 JWT를 검증하고
 * 인증 정보(X-User-Id, X-User-Role)를 다운스트림 헤더에 주입
 * 다운스트림 서비스는 JWT를 직접 검증하지 않고 이 헤더만 신뢰
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final GatewayServiceProperties properties;
    private final JWTUtil jwtUtil;
    private final JsonMapper jsonMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (shouldSkipAuthentication(path, method)) {
            return chain.filter(exchange);
        }

        String token = extractAccessToken(request);
        if (token == null) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_MISSING", "토큰이 존재하지 않습니다.");
        }

        Claims claims;
        try {
            claims = jwtUtil.validateAndGetClaims(token);
        } catch (ExpiredJwtException e) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "토큰이 유효하지 않습니다.");
        }

        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(role)) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "토큰이 유효하지 않습니다.");
        }

        String normalizedRole = normalizeRole(role);
        if (isAdminPath(path) && !isAdminRole(normalizedRole)) {
            return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
        }

        ServerHttpRequest mutated = request.mutate()
                .header(GatewayHeaders.USER_ID, userId)
                .header(GatewayHeaders.USER_ROLE, normalizedRole)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean shouldSkipAuthentication(String path, HttpMethod method) {
        if (!path.startsWith("/api/")) {
            return true;
        }
        if (isPublicPath(path)) {
            return true;
        }
        return HttpMethod.GET.equals(method) && isPublicGetPath(path);
    }

    private boolean isPublicPath(String path) {
        return matchesAny(path, properties.getPublicPaths());
    }

    private boolean isPublicGetPath(String path) {
        return matchesAny(path, properties.getPublicGetPaths());
    }

    private boolean isAdminPath(String path) {
        String prefix = properties.getAdminApiPathPrefix();
        return StringUtils.hasText(prefix) && path.startsWith(prefix);
    }

    private boolean isAdminRole(String role) {
        return properties.getAdminRequiredRole().equals(role);
    }

    private boolean matchesAny(String path, List<String> patterns) {
        for (String pattern : patterns) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            if (containsWildcard(pattern)) {
                if (pathMatcher.match(pattern, path)) {
                    return true;
                }
                continue;
            }
            if (path.equals(pattern) || path.startsWith(pattern + "/")) {
                return true;
            }
        }
        return false;
    }

    private String extractAccessToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization)) {
            if (!authorization.startsWith(BEARER_PREFIX)) {
                return null;
            }
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            return StringUtils.hasText(token) ? token : null;
        }

        HttpCookie cookie = request.getCookies().getFirst(ACCESS_TOKEN_COOKIE);
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue();
        }
        return null;
    }

    private boolean containsWildcard(String pattern) {
        return pattern.contains("*") || pattern.contains("?") || pattern.contains("{") || pattern.contains("}");
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "status", status.value(),
                "code", code,
                "message", message
        );

        byte[] bytes;
        bytes = jsonMapper.writeValueAsBytes(body);

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
