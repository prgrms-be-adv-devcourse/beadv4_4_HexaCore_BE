package com.back.detector.app;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.CrawlingDetectResult;
import com.back.detector.domain.DetectorPolicy;
import com.back.detector.domain.enums.DetectorRedisKey;
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
     * 이미 차단 중인 경우에도 예외를 직접 던지지 않고 DetectResult를 반환해 Facade에서 로그 저장 후 예외 처리
     */
    public CrawlingDetectResult checkCrawling(String ip) {
        CrawlingDetectResult bannedResult = getBannedResult(ip);
        if (bannedResult != null) {
            return bannedResult;
        }

        String countKey = DetectorRedisKey.CRAWLING_COUNT.getKey(ip);
        Long newCount = detectorRedisTemplate.opsForValue().increment(countKey);

        detectorRedisTemplate.expire(countKey,
                DetectorPolicy.CRAWLING.getTimeWindowMinutes(),
                TimeUnit.MINUTES);

        if (newCount > DetectorPolicy.CRAWLING.getMaxAttempts()) {
            CrawlingBanLevel level = applyBan(ip);
            return new CrawlingDetectResult(level, newCount.intValue());
        }
        return null;
    }

    /**
     * 이미 차단 중인 경우 ban 키에 저장된 단계를 읽어 DetectResult로 반환
     * 예외를 직접 던지지 않고 Facade에서 일괄 처리하도록 위임
     */
    private CrawlingDetectResult getBannedResult(String ip) {
        String banKey = DetectorRedisKey.CRAWLING_BAN.getKey(ip);
        String storedLevel = detectorRedisTemplate.opsForValue().get(banKey);
        if (storedLevel == null) {
            return null;
        }
        CrawlingBanLevel level = CrawlingBanLevel.valueOf(storedLevel);
        return new CrawlingDetectResult(level, 0);
    }

    /**
     * 차단 카운트를 증가시키고, 단계별 차단 시간을 적용
     * FIRST(5분) → SECOND(1시간)
     */
    private CrawlingBanLevel applyBan(String ip) {
        String banKey = DetectorRedisKey.CRAWLING_BAN.getKey(ip);
        String countKey = DetectorRedisKey.CRAWLING_COUNT.getKey(ip);

        Long banCount = detectorRedisTemplate.opsForValue().increment(DetectorRedisKey.CRAWLING_BAN_COUNT.getKey(ip));

        CrawlingBanLevel level = CrawlingBanLevel.of(banCount);

        // ban 키에 banLevel 값을 저장해 차단 중 재진입 시에도 단계 정보 조회 가능
        detectorRedisTemplate.opsForValue().set(banKey, level.name());
        detectorRedisTemplate.expire(banKey, level.getBanMinutes(), TimeUnit.MINUTES);

        detectorRedisTemplate.delete(countKey);

        log.warn("크롤링 봇 차단 - IP: {}, 차단 단계: {}, 차단 시간: {}분", ip, level.name(), level.getBanMinutes());
        return level;
    }
}
