package com.back.detector.app;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamDetectResult;
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
public class BidSpamDetector {
    private final RedisTemplate<String, String> detectorRedisTemplate;

    public BidSpamDetectResult checkBidSpam(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 null일 수 없습니다");
        }

        BidSpamDetectResult bannedResult = getBannedResult(userId);
        if (bannedResult != null) {
            return bannedResult;
        }

        String countKey = DetectorRedisKey.BID_COUNT.getKey(userId);
        Long newCount = detectorRedisTemplate.opsForValue().increment(countKey);

        if (newCount == 1) {
            detectorRedisTemplate.expire(countKey,
                    DetectorPolicy.BID_SPAM.getTimeWindowMinutes(),
                    TimeUnit.MINUTES);
        }

        if (newCount > DetectorPolicy.BID_SPAM.getMaxAttempts()) {
            return new BidSpamDetectResult(applyBan(userId, newCount), newCount);
        }

        return null;
    }

    /**
     * 이미 차단 중인 경우 ban 키에 저장된 단계를 읽어 DetectResult로 반환
     * 예외를 직접 던지지 않고 Facade에서 일괄 처리하도록 위임
     */
    private BidSpamDetectResult getBannedResult(Long userId) {
        String banKey = DetectorRedisKey.BID_BAN.getKey(userId);
        String storedLevel = detectorRedisTemplate.opsForValue().get(banKey);
        if (storedLevel == null) {
            return null;
        }
        BidSpamBanLevel level = BidSpamBanLevel.valueOf(storedLevel);
        return new BidSpamDetectResult(level, 0L);
    }

    private BidSpamBanLevel applyBan(Long userId, Long requestCount) {
        String banKey = DetectorRedisKey.BID_BAN.getKey(userId);
        String countKey = DetectorRedisKey.BID_COUNT.getKey(userId);

        String banCountKey = DetectorRedisKey.BID_BAN_COUNT.getKey(userId);
        Long banCount = detectorRedisTemplate.opsForValue().increment(banCountKey);

        BidSpamBanLevel level = BidSpamBanLevel.of(banCount);

        // ban 키에 banLevel 값을 저장해 차단 중 재진입 시에도 단계 정보 조회 가능
        detectorRedisTemplate.opsForValue().set(banKey, level.name());
        detectorRedisTemplate.expire(banKey, level.getBanMinutes(), TimeUnit.MINUTES);

        detectorRedisTemplate.delete(countKey);

        log.warn("[입찰 스팸 차단] userId: {}, 차단 단계: {}, 차단 시간: {}분", userId, level.name(), level.getBanMinutes());
        return level;
    }
}
