package com.back.user.adapter.in.auth.security.handler;

import com.back.security.jwt.JWTUtil;
import com.back.user.adapter.in.auth.security.oauth.principal.CustomOAuth2User;
import com.back.user.adapter.out.RefreshStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshStore refreshStore;

    @Value("${app.jwt.refresh-ttl}")
    private Duration refreshTtl;

    @Value("${app.frontend.callback-url}")
    private String frontendCallbackUrl;

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
        response.addCookie(createCookie("refresh", refreshToken, refreshTtl));

        // 프론트 경로로 redirect
        response.sendRedirect(frontendCallbackUrl);
    }

    private Cookie createCookie(String key, String value, Duration ttl) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge((int) ttl.getSeconds());
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}

