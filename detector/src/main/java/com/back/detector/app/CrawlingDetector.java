package com.back.detector.app;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.DetectorPolicy;
import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.exception.CrawlingDetectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingDetector {

    private final RedisTemplate<String, String> detectorRedisTemplate;

    /**
     * IP 기반 크롤링 감지
     * 1단계: 1분 내 조회 횟수를 카운팅
     * 2단계: 횟수 초과 시 차단 카운트를 증가하고, 단계별 차단 시간 적용
     */
    public CrawlingBanLevel checkCrawling(String ip) {
        if (isBanned(ip)) {
            throw new CrawlingDetectedException();
        }

        String countKey = DetectorRedisKey.CRAWLING_COUNT.getKey(ip);
        Long newCount = detectorRedisTemplate.opsForValue().increment(countKey);

        detectorRedisTemplate.expire(countKey,
                DetectorPolicy.CRAWLING.getTimeWindowMinutes(),
                TimeUnit.MINUTES);

        if (newCount > DetectorPolicy.CRAWLING.getMaxAttempts()) {
            CrawlingBanLevel level = applyBan(ip);
            return level;
        }
        return null;
    }

    /**
     * 현재 IP가 차단 중인지 확인
     */
    private boolean isBanned(String ip) {
        String banKey = DetectorRedisKey.CRAWLING_BAN.getKey(ip);
        return Boolean.TRUE.equals(detectorRedisTemplate.hasKey(banKey));
    }

    /**
     * 차단 카운트를 증가시키고, 단계별 차단 시간을 적용
     * FIRST(5분) → SECOND(1시간)
     */
    private CrawlingBanLevel applyBan(String ip) {
        String banKey = DetectorRedisKey.CRAWLING_BAN.getKey(ip);
        String countKey = DetectorRedisKey.CRAWLING_COUNT.getKey(ip);

        // 차단 횟수 카운트 키 (만료 없이 유지)
        String banCountKey = banKey + ":count";
        Long banCount = detectorRedisTemplate.opsForValue().increment(banCountKey);

        CrawlingBanLevel level = CrawlingBanLevel.of(banCount);

        detectorRedisTemplate.opsForValue().set(banKey, "banned");
        detectorRedisTemplate.expire(banKey, level.getBanMinutes(), TimeUnit.MINUTES);

        // 조회 카운트 키 초기화
        detectorRedisTemplate.delete(countKey);

        log.warn("크롤링 봇 차단 - IP: {}, 차단 단계: {}, 차단 시간: {}분", ip, level.name(), level.getBanMinutes());
        return level;
    }
}
