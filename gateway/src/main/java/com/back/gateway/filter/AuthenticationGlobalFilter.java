package com.back.gateway.filter;

import com.back.common.code.FailureCode;
import com.back.common.exception.ForbiddenException;
import com.back.common.exception.UnauthorizedException;
import com.back.gateway.config.GatewayServiceProperties;
import com.back.security.header.GatewayHeaders;
import com.back.security.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (shouldSkipAuthentication(path, method)) {
            return chain.filter(exchange);
        }

        Claims claims = validateToken(request);
        checkAdminAuthority(path, claims);
        ServerHttpRequest mutated = addHeaders(request, claims);

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

    private Claims validateToken(ServerHttpRequest request) {
        String token = extractAccessToken(request);
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException(FailureCode.TOKEN_MISSING);
        }

        try {
            Claims claims = jwtUtil.validateAndGetClaims(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (!StringUtils.hasText(userId) || !StringUtils.hasText(role)) {
                throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(FailureCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
        }
    }

    private void checkAdminAuthority(String path, Claims claims) {
        String role = normalizeRole(claims.get("role", String.class));
        if (isAdminPath(path) && !isAdminRole(role)) {
            throw new ForbiddenException(FailureCode.FORBIDDEN);
        }
    }

    private ServerHttpRequest addHeaders(ServerHttpRequest request, Claims claims) {
        String userId = claims.getSubject();
        String role = normalizeRole(claims.get("role", String.class));

        return request.mutate()
                .header(GatewayHeaders.USER_ID, userId)
                .header(GatewayHeaders.USER_ROLE, role)
                .build();
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
                throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
            }
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            if (!StringUtils.hasText(token)) {
                throw new UnauthorizedException(FailureCode.TOKEN_INVALID);
            }
            return token;
        }

        HttpCookie cookie = request.getCookies().getFirst(ACCESS_TOKEN_COOKIE);
        return cookie != null ? cookie.getValue() : "";
    }

    private boolean containsWildcard(String pattern) {
        return pattern.contains("*") || pattern.contains("?") || pattern.contains("{") || pattern.contains("}");
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
