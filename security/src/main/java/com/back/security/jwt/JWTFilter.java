package com.back.security.jwt;

import com.back.security.principal.AuthPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT를 검증하기 위한 커스텀 필터
 */
@RequiredArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = extractToken(request);

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null  ) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            Claims claims = jwtUtil.validateAndGetClaims(accessToken);
            setAuthenticationContext(claims);

        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private void setAuthenticationContext(Claims claims) {
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);

        AuthPrincipal principal = new AuthPrincipal(userId, role);
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}

