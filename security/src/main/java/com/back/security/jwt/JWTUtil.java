package com.back.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * jwt를 발급 및 검증하는 클래스
 */
@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * JWT 토큰에서 userId 가져오기
     */
    public Long getUserId(String token) {
        String sub = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
        return Long.parseLong(sub);
    }

    /**
     * JWT 토큰에서 role 가져오기
     */
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * JWT 토큰 검증 및 데이터 추출
     */
    public Claims validateAndGetClaims(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();

        // 카테고리 검증
        String category = claims.get("category", String.class);
        if (!"access".equals(category)) {
            throw new JwtException("Access 토큰이 아닙니다.");
        }

        return claims;
    }

    /**
     * JWT 토큰에서 어떤 토큰인지 가져오기
     */
    public String getCategory(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    /**
     * JWT 토큰 생성하기
     */
    public String createJwt(String category, Long userId, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("category", category)
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}

