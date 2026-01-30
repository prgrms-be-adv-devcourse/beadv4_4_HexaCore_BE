package com.back.user.app.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieSupport {

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
