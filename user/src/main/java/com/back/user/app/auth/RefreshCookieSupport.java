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
                .sameSite("Lax")
                .maxAge(ttl)
                .build()
                .toString();
    }


    public String deleteRefreshCookieHeader() {
        return ResponseCookie.from("refresh", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build()
                .toString();
    }
}
