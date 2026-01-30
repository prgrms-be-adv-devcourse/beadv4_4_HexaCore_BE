package com.back.user.adapter.in.auth.security.handler;

import com.back.security.jwt.JWTUtil;
import com.back.user.adapter.in.auth.security.config.OAuth2UriConfig;
import com.back.user.adapter.in.auth.security.oauth.principal.CustomOAuth2User;
import com.back.user.adapter.out.RefreshStore;
import com.back.user.app.auth.RefreshCookieSupport;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String REDIRECT_URI_COOKIE = "redirect_uri";

    private final JWTUtil jwtUtil;
    private final RefreshStore refreshStore;
    private final RefreshCookieSupport refreshCookieSupport;

    @Value("${app.jwt.refresh-ttl}")
    private Duration refreshTtl;

    @Value("${app.frontend.callback-url}")
    private String frontendCallbackUrl;

    private final OAuth2UriConfig oauth2UriConfig;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = customUserDetails.getUserId();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // refresh 토큰 생성
        String refreshToken = jwtUtil.createJwt("refresh", userId, role, refreshTtl.toMillis());

        // 로그인, 회원가입 완료시 저장
        refreshStore.save(userId, refreshToken, refreshTtl);

        // 쿠키에 refresh 토큰 저장
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieSupport.createRefreshSetCookieHeader(refreshToken, refreshTtl));

        // redirect 기본값
        String targetUrl = frontendCallbackUrl;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (REDIRECT_URI_COOKIE.equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    String candidate = c.getValue();

                    // 화이트리스트 검증
                    if (isAllowedRedirect(candidate)) {
                        targetUrl = candidate;
                    }
                    break;
                }
            }
        }

        // redirect_uri 쿠키 삭제
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieSupport.deleteRedirectUriCookieHeader());

        // 프론트 경로로 redirect
        response.sendRedirect(targetUrl);
    }

    private boolean isAllowedRedirect(String uri) {
        if (!StringUtils.hasText(uri)) return false;
        return oauth2UriConfig.getAuthorizedRedirectUris().contains(uri.trim());
    }

}

