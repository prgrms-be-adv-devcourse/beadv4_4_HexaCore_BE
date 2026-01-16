package com.back.security.config;

import com.back.security.jwt.JWTFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class BaseSecurityConfig {

    private final JWTFilter jwtFilter;

    @Bean
    @Order(3)
    public SecurityFilterChain apiJwtChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/api/v1/**");

        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(cache -> cache.disable());

        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        // JWT 인증 필터
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return (HttpServletRequest request) -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(List.of(
                    "http://localhost:*",
                    "http://127.0.0.1:*"
            ));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            configuration.setAllowCredentials(true);
            configuration.setAllowedHeaders(Collections.singletonList("*"));
            configuration.setMaxAge(3600L);
            configuration.setExposedHeaders(List.of("Authorization"));
            return configuration;
        };
    }
}
