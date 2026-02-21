package com.back.detector.app;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamDetectResult;
import com.back.detector.domain.DetectorPolicy;
import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.exception.BidSpamException;
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

        if (isBanned(userId)) {
            throw new BidSpamException();
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

    private boolean isBanned(Long userId) {
        String banKey = DetectorRedisKey.BID_BAN.getKey(userId);
        return Boolean.TRUE.equals(detectorRedisTemplate.hasKey(banKey));
    }

    private BidSpamBanLevel applyBan(Long userId, Long requestCount) {
        String banKey = DetectorRedisKey.BID_BAN.getKey(userId);
        String countKey = DetectorRedisKey.BID_COUNT.getKey(userId);

        String banCountKey = DetectorRedisKey.BID_BAN_COUNT.getKey(userId);
        Long banCount = detectorRedisTemplate.opsForValue().increment(banCountKey);

        BidSpamBanLevel level = BidSpamBanLevel.of(banCount);

        detectorRedisTemplate.opsForValue().set(banKey, "banned");
        detectorRedisTemplate.expire(banKey, level.getBanMinutes(), TimeUnit.MINUTES);

        detectorRedisTemplate.delete(countKey);

        log.warn("[입찰 스팸 차단] userId: {}, 차단 단계: {}, 차단 시간: {}분", userId, level.name(), level.getBanMinutes());
        return level;
    }
}
