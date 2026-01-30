package com.back.user.app.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieSupport {

    public String createRefreshSetCookieHeader(String refreshToken, Duration ttl) {
        return ResponseCookie.from("refresh", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(ttl)
                .build()
                .toString();
    }


    public String deleteRefreshCookieHeader() {
        return ResponseCookie.from("refresh", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build()
                .toString();
    }

    // 리다이렉트 URI 저장을 위한 쿠키 생성
    public String createRedirectUriCookieHeader(String redirectUri) {
        return ResponseCookie.from("redirect_uri", redirectUri)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofMinutes(5))
                .build()
                .toString();
    }

    // 리다이렉트 URI 쿠키 삭제
    public String deleteRedirectUriCookieHeader() {
        return ResponseCookie.from("redirect_uri", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build()
                .toString();
    }
}
