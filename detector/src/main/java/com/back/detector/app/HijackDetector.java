package com.back.detector.app;

import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.exception.HijackDetectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class HijackDetector {

    private final RedisTemplate<String, String> detectorRedisTemplate;

    private static final long NEW_IP_TRANSACTION_LIMIT = 200_000L;
    private static final int IP_HISTORY_DAYS = 90;
    private static final int NEW_IP_COOLDOWN_HOURS = 24;
    private static final int MAX_IP_COUNT = 10;
    private static final int BAN_IP_DAYS = 1;


    /**
     * 회원가입 시 첫 IP를 안전한 IP로 등록
     */
    public void registerInitialIp(Long userId, String ip) {
        String ipSetKey = DetectorRedisKey.TRUSTED_IP.getKey(userId);

        detectorRedisTemplate.opsForSet().add(ipSetKey, ip);
        detectorRedisTemplate.expire(ipSetKey, IP_HISTORY_DAYS, TimeUnit.DAYS);

        log.info("[회원가입 IP 등록] userId: {}, IP: {} (쿨다운 없이 즉시 신뢰)", userId, ip);
    }

    /**
     * 계정 탈취 감지
     * 
     * Redis 키 구조:
     * - trusted:ips:{userId}: 신뢰 IP 목록 (Set, TTL: 90일)
     * - new:ip:timestamp:{userId}:{ip}: 새 IP 등록 시각 (String, TTL: 24시간)
     * - new:ip:amount:{userId}:{ip}: 새 IP 누적 거래 금액 (String, TTL: 24시간)
     * 
     * 규칙:
     * - 새 IP: 24시간 동안 총 20만원까지만 허용
     * - 신뢰 IP: 제한 없음
     */
    public void checkHijack(Long userId, String currentIp, Long transactionAmount) {
        if (isUserBlocked(userId)) {
            throw new HijackDetectedException();
        }

        String trustedIpSetKey = DetectorRedisKey.TRUSTED_IP.getKey(userId);
        String ipTimestampKey = DetectorRedisKey.NEW_IP_TIMESTAMP.getKey(userId, currentIp);
        String accumulatedAmountKey = DetectorRedisKey.NEW_IP_AMOUNT.getKey(userId, currentIp);

        // trusted:ips:{userId}에서 현재 IP 확인
        Boolean isKnownIp = detectorRedisTemplate.opsForSet().isMember(trustedIpSetKey, currentIp);

        // 1. 완전히 새로운 IP
        if (Boolean.FALSE.equals(isKnownIp)) {
            // 첫 거래 한도 체크
            if (transactionAmount > NEW_IP_TRANSACTION_LIMIT) {
                blockUser(userId);
                notifyAdmin(userId, getAllIps(userId), currentIp, transactionAmount,
                        NEW_IP_TRANSACTION_LIMIT, null, null);
                throw new HijackDetectedException();
            }

            // IP를 신뢰 목록에 추가 (최대 10개까지만)
            addIpToHistory(userId, currentIp, trustedIpSetKey);

            // 쿨다운 시작: 24시간 타임스탬프 저장
            detectorRedisTemplate.opsForValue().set(
                    ipTimestampKey,
                    String.valueOf(System.currentTimeMillis()),
                    NEW_IP_COOLDOWN_HOURS,
                    TimeUnit.HOURS
            );

            // 쿨다운 시작: 24시간 누적 금액 초기화
            detectorRedisTemplate.opsForValue().set(
                    accumulatedAmountKey,
                    String.valueOf(transactionAmount),
                    NEW_IP_COOLDOWN_HOURS,
                    TimeUnit.HOURS
            );

            log.info("[새 IP 등록] userId: {}, IP: {}, 첫 거래: {}원, 잔여: {}원",
                    userId, currentIp, transactionAmount,
                    NEW_IP_TRANSACTION_LIMIT - transactionAmount);
            return;
        }

        // 2. 알려진 IP
        String timestamp = detectorRedisTemplate.opsForValue().get(ipTimestampKey);

        // 2-1. 쿨다운 기간 중 (타임스탬프 키 존재 = 24시간 이내)
        if (timestamp != null) {
            // 누적 한도
            Long newTotal = detectorRedisTemplate.opsForValue().increment(accumulatedAmountKey, transactionAmount);

            if (newTotal == null) {
                log.error("[누적 금액 키 없음] userId: {}, IP: {}", userId, currentIp);
                newTotal = transactionAmount;
            }

            // 누적 한도 체크
            if (newTotal > NEW_IP_TRANSACTION_LIMIT) {
                long registeredTime = Long.parseLong(timestamp);
                long hoursPassed = (System.currentTimeMillis() - registeredTime) / (1000 * 60 * 60);

                blockUser(userId);
                notifyAdmin(userId, getAllIps(userId), currentIp, transactionAmount,
                        NEW_IP_TRANSACTION_LIMIT, newTotal - transactionAmount, hoursPassed);
                throw new HijackDetectedException();
            }

            log.info("[쿨다운 중 거래] userId: {}, IP: {}, 현재: {}원, 누적: {}원, 잔여: {}원",
                    userId, currentIp, transactionAmount, newTotal,
                    NEW_IP_TRANSACTION_LIMIT - newTotal);
            return;
        }

        // 2-2. 쿨다운 종료 (타임스탬프 키 만료 = 24시간 경과) → 신뢰 IP
        log.debug("[신뢰 IP 거래] userId: {}, IP: {}, 금액: {}원 (제한 없음)",
                userId, currentIp, transactionAmount);
    }

    private boolean isUserBlocked(Long userId) {
        String blockKey = DetectorRedisKey.USER_BLOCKED.getKey(userId);
        String blocked = detectorRedisTemplate.opsForValue().get(blockKey);
        return "true".equals(blocked);
    }

    /**
     * IP를 신뢰 목록에 추가 (최대 10개 제한)
     */
    private void addIpToHistory(Long userId, String currentIp, String ipSetKey) {
        Long ipCount = detectorRedisTemplate.opsForSet().size(ipSetKey);

        // 최대 IP 개수 초과 시 차단 (탈취 의심)
        if (ipCount != null && ipCount >= MAX_IP_COUNT) {
            log.warn("[IP 개수 제한 초과] userId: {}, 현재 개수: {}, 최대: {}",
                    userId, ipCount, MAX_IP_COUNT);
            throw new HijackDetectedException();
        }

        detectorRedisTemplate.opsForSet().add(ipSetKey, currentIp);
        detectorRedisTemplate.expire(ipSetKey, IP_HISTORY_DAYS, TimeUnit.DAYS);
    }

    private String getAllIps(Long userId) {
        String ipSetKey = DetectorRedisKey.TRUSTED_IP.getKey(userId);
        Set<String> ips = detectorRedisTemplate.opsForSet().members(ipSetKey);
        return ips == null || ips.isEmpty() ? "없음" : String.join(", ", ips);
    }

    private void notifyAdmin(Long userId, String existingIps, String currentIp, Long currentAmount,
                             Long limit, Long previousAmount, Long hoursPassed) {
        String reason;

        if (previousAmount == null) {
            // 새 IP 첫 거래 한도 초과
            reason = String.format("새 IP 첫 거래 한도 초과 (한도: %,d원)", limit);
        } else {
            // 새 IP 누적 금액 초과
            reason = String.format("새 IP 누적 금액 초과 (기존: %,d원 + 현재: %,d원 = %,d원, 한도: %,d원, 등록 후 %d시간)",
                    previousAmount, currentAmount, previousAmount + currentAmount, limit, hoursPassed);
        }

        log.warn("""
            [관리자 알림] 계정 탈취 의심
            - 사용자ID: {}
            - 기존IP 목록: {}
            - 현재IP: {}
            - 거래금액: {}원
            - 차단사유: {}
            """, userId, existingIps, currentIp, currentAmount, reason);
    }

    private void blockUser(Long userId) {
        String blockKey = DetectorRedisKey.USER_BLOCKED.getKey(userId);
        detectorRedisTemplate.opsForValue().set(blockKey, "true", BAN_IP_DAYS, TimeUnit.DAYS);
        log.error("[계정 차단] 사용자 ID: {} (24시간 차단)", userId);
    }
}
