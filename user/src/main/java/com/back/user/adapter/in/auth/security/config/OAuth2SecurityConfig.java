package com.back.user.adapter.in.auth.security.config;


import com.back.user.adapter.in.auth.security.handler.CustomSuccessHandler;
import com.back.user.adapter.in.auth.security.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class OAuth2SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2Chain(HttpSecurity http) throws Exception {
        http.securityMatcher("/oauth2/**", "/login/**");

        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
        );

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
}
