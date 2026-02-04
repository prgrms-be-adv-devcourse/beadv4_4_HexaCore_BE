package com.back.user.adapter.in.auth.security.config;


import com.back.user.adapter.in.auth.security.filter.RedirectUriCookieFilter;
import com.back.user.adapter.in.auth.security.handler.CustomSuccessHandler;
import com.back.user.adapter.in.auth.security.oauth.CustomOAuth2UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class OAuth2SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final RedirectUriCookieFilter redirectUriCookieFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2Chain(HttpSecurity http) throws Exception {
        http.securityMatcher("/oauth2/**", "/login/**");
        http.cors(withDefaults());
        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        http.addFilterBefore(redirectUriCookieFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("[OAUTH2 FAIL] uri={} query={} msg={}",
                            request.getRequestURI(),
                            request.getQueryString(),
                            exception.getMessage(),
                            exception);

                    String redirectUri = getCookieValue(request, "redirect_uri")
                            .filter(s -> !s.isBlank())
                            .orElse("http://localhost:5173/auth/callback");

                    String error = exception.getClass().getSimpleName() + ":" +
                            (exception.getMessage() == null ? "" : exception.getMessage());

                    String target = redirectUri + (redirectUri.contains("?") ? "&" : "?")
                            + "oauth_error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);

                    response.sendRedirect(target);
                })
        );

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    private static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
