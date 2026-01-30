package com.back.user.adapter.in.auth.security.filter;

import com.back.user.app.auth.RefreshCookieSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RedirectUriCookieFilter extends OncePerRequestFilter {

    private final RefreshCookieSupport refreshCookieSupport;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String redirectUri = request.getParameter("redirect_uri");
        if (StringUtils.hasText(redirectUri)) {
            response.addHeader(HttpHeaders.SET_COOKIE,
                    refreshCookieSupport.createRedirectUriCookieHeader(redirectUri));
        }

        filterChain.doFilter(request, response);
    }
}
