package com.back.security.util;

import com.back.security.principal.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityHelper {
    
    public static Long getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof AuthPrincipal)
                .map(principal -> ((AuthPrincipal) principal).getUserId())
                .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다"));
    }
    
    public static AuthPrincipal getCurrentPrincipal() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof AuthPrincipal)
                .map(principal -> (AuthPrincipal) principal)
                .orElseThrow(() -> new IllegalStateException("인증 정보가 없습니다"));
    }
}
