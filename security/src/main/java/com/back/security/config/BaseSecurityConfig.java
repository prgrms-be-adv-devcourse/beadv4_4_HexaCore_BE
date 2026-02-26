package com.back.security.config;

import com.back.security.filter.GatewayHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class BaseSecurityConfig {
    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    @Order(100)
    public SecurityFilterChain apiJwtChain(HttpSecurity http) {

        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());

        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**",
                        "/ws/**",
                        "/api/v1/internal/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/market/products/**").permitAll()
                .anyRequest().authenticated()
        );

        // Gateway가 주입한 X-User-Id, X-User-Role 헤더로 SecurityContext 설정
        http.addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
