package com.back.user.adapter.in.auth.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class ReissueSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain reissueChain(HttpSecurity http) throws Exception {

        // reissue만 이 체인이 담당
        http.securityMatcher("/api/v1/reissue");

        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/reissue").permitAll()
                .anyRequest().denyAll()
        );

        return http.build();
    }
}
