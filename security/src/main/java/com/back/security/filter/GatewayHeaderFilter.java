package com.back.security.filter;

import com.back.security.header.GatewayHeaders;
import com.back.security.principal.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Gateway가 주입한 X-User-Id, X-User-Role 헤더를 읽어 SecurityContext에 인증 정보를 설정
 * JWT 검증은 Gateway(AuthenticationGlobalFilter)에서 수행
 * 내부 서비스는 이 필터를 통해 헤더 기반으로 인증 정보를 수신
 */
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(GatewayHeaders.USER_ID);
        String role = request.getHeader(GatewayHeaders.USER_ROLE);

        if (hasAuthHeaders(userId, role)) {
            Long parsedUserId = parseUserId(userId);
            if (parsedUserId != null) {
                setSecurityContext(parsedUserId, role);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasAuthHeaders(String userId, String role) {
        return StringUtils.hasText(userId) && StringUtils.hasText(role);
    }

    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setSecurityContext(long userId, String role) {
        AuthPrincipal principal = new AuthPrincipal(userId, role);
        Authentication authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
