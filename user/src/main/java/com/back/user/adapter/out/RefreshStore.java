package com.back.user.adapter.out;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshStore {

    private final StringRedisTemplate redis;

    private String key(Long userId){
        return "refresh:u:" + userId;
    }

    /** 로그인/회원가입 완료 시 저장 */
    public void save(Long userId, String refreshToken, Duration ttl) {
        redis.opsForValue().set(key(userId), refreshToken, ttl);
    }

    /** 재발급 시, 요청 토큰이 현재 저장된 토큰과 같은지 확인 */
    public boolean isValid(Long userId, String refreshToken) {
        String stored = redis.opsForValue().get(key(userId));
        return refreshToken != null && refreshToken.equals(stored);
    }

    /** 로테이트(새 토큰으로 교체 + TTL 갱신) */
    public void rotate(Long userId, String newRefreshToken, Duration ttl) {
        redis.opsForValue().set(key(userId), newRefreshToken, ttl);
    }

    /** 로그아웃/전면 폐기 */
    public void revoke(Long userId) {
        redis.delete(key(userId));
    }
}

