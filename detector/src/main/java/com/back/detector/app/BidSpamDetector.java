package com.back.detector.app;

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

    public void checkBidSpam(Long userId) {
        String key = DetectorRedisKey.BID_COUNT.getKey(userId);
        Long newCount = detectorRedisTemplate.opsForValue().increment(key);

        detectorRedisTemplate.expire(key,
                DetectorPolicy.BID_SPAM.getTimeWindowMinutes(),
                TimeUnit.MINUTES);

        if(newCount > DetectorPolicy.BID_SPAM.getMaxAttempts()) {
            // TODO: event를 사용한 경고, 밴 구현
            throw new BidSpamException();
        }

    }

}
