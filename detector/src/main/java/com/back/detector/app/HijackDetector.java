package com.back.detector.app;

import com.back.detector.domain.enums.DetectorRedisKey;
import com.back.detector.dto.HijackDetectedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class HijackDetector {

    private final RedisTemplate<String, String> detectorRedisTemplate;

    private static final BigDecimal NEW_IP_TRANSACTION_LIMIT = new BigDecimal("200000");
    private static final int IP_HISTORY_DAYS = 90;
    private static final int NEW_IP_COOLDOWN_HOURS = 24;

    private final ApplicationEventPublisher applicationEventPublisher;

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
     * - 새 IP 첫 거래가 20만원 이하일 때만 IP 등록
     * - 새 IP: 24시간 동안 총 20만원 초과 시 이메일 알림 발송
     * - 신뢰 IP: 제한 없음
     * - 모든 거래는 항상 허용됨 (차단 없음)
     */
    @Transactional
    public void checkHijack(Long userId, String userEmail, String currentIp, BigDecimal transactionAmount) {
        String trustedIpSetKey = DetectorRedisKey.TRUSTED_IP.getKey(userId);
        String ipTimestampKey = DetectorRedisKey.NEW_IP_TIMESTAMP.getKey(userId, currentIp);
        String accumulatedAmountKey = DetectorRedisKey.NEW_IP_AMOUNT.getKey(userId, currentIp);

        Boolean isKnownIp = detectorRedisTemplate.opsForSet().isMember(trustedIpSetKey, currentIp);

        // 1. 완전히 새로운 IP
        if (!Boolean.TRUE.equals(isKnownIp)) {
            // 첫 거래가 한도 초과하면 IP 등록하지 않고 메일만 발송
            if (transactionAmount.compareTo(NEW_IP_TRANSACTION_LIMIT) > 0) {
                sendEmailNotification(userId, userEmail, getAllIps(userId), currentIp, 
                        transactionAmount, NEW_IP_TRANSACTION_LIMIT, null, null);
                
                log.warn("[새 IP 거부] userId: {}, IP: {}, 첫 거래: {}원 (한도 {}원 초과로 IP 등록 안함)",
                        userId, currentIp, transactionAmount, NEW_IP_TRANSACTION_LIMIT);
                return;
            }

            // 첫 거래가 한도 이하일 때만 IP 신뢰 목록에 등록
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
                    transactionAmount.toString(),
                    NEW_IP_COOLDOWN_HOURS,
                    TimeUnit.HOURS
            );

            BigDecimal remaining = NEW_IP_TRANSACTION_LIMIT.subtract(transactionAmount);
            log.info("[새 IP 등록] userId: {}, IP: {}, 첫 거래: {}원, 잔여: {}원",
                    userId, currentIp, transactionAmount, remaining.max(BigDecimal.ZERO));
            return;
        }

        // 2. 알려진 IP
        String timestamp = detectorRedisTemplate.opsForValue().get(ipTimestampKey);

        // 2-1. 쿨다운 기간 중 (타임스탬프 키 존재 = 24시간 이내)
        if (timestamp != null) {
            // 기존 누적 금액 조회
            String currentAccumulatedStr = detectorRedisTemplate.opsForValue().get(accumulatedAmountKey);
            BigDecimal currentAccumulated = currentAccumulatedStr != null 
                    ? new BigDecimal(currentAccumulatedStr) 
                    : BigDecimal.ZERO;

            // 거래 금액만큼 누적 (increment 사용 - TTL은 최초 등록 시점 기준 유지)
            Long incrementedValue = detectorRedisTemplate.opsForValue()
                    .increment(accumulatedAmountKey, transactionAmount.longValue());
            
            BigDecimal newTotal = incrementedValue != null 
                    ? new BigDecimal(incrementedValue) 
                    : currentAccumulated.add(transactionAmount);

            // 누적 한도 초과하면 메일 발송
            if (newTotal.compareTo(NEW_IP_TRANSACTION_LIMIT) > 0) {
                long registeredTime = Long.parseLong(timestamp);
                long hoursPassed = (System.currentTimeMillis() - registeredTime) / (1000 * 60 * 60);

                sendEmailNotification(userId, userEmail, getAllIps(userId), currentIp, 
                        transactionAmount, NEW_IP_TRANSACTION_LIMIT, currentAccumulated, hoursPassed);
            }

            BigDecimal remaining = NEW_IP_TRANSACTION_LIMIT.subtract(newTotal);
            log.info("[쿨다운 중 거래] userId: {}, IP: {}, 현재: {}원, 누적: {}원, 잔여: {}원",
                    userId, currentIp, transactionAmount, newTotal, remaining.max(BigDecimal.ZERO));
            return;
        }

        // 2-2. 쿨다운 종료 (타임스탬프 키 만료 = 24시간 경과) → 신뢰 IP
        log.debug("[신뢰 IP 거래] userId: {}, IP: {}, 금액: {}원 (제한 없음)",
                userId, currentIp, transactionAmount);
    }

    /**
     * IP를 신뢰 목록에 추가
     */
    private void addIpToHistory(Long userId, String currentIp, String ipSetKey) {
        detectorRedisTemplate.opsForSet().add(ipSetKey, currentIp);
        detectorRedisTemplate.expire(ipSetKey, IP_HISTORY_DAYS, TimeUnit.DAYS);
    }

    /**
     * 사용자의 모든 IP 목록 조회
     */
    private String getAllIps(Long userId) {
        String ipSetKey = DetectorRedisKey.TRUSTED_IP.getKey(userId);
        Set<String> ips = detectorRedisTemplate.opsForSet().members(ipSetKey);
        return ips == null || ips.isEmpty() ? "없음" : String.join(", ", ips);
    }

    /**
     * 이메일 알림 발송
     */
    @Transactional
    public void sendEmailNotification(Long userId, String userEmail, String existingIps,
                                      String currentIp, BigDecimal currentAmount,
                                      BigDecimal limit, BigDecimal previousAmount, Long hoursPassed) {
        String reason;

        if (previousAmount == null) {
            // 새 IP 첫 거래 한도 초과
            reason = String.format("새 IP 첫 거래 한도 초과 (한도: %,d원, 거래: %,d원)", 
                    limit.longValue(), currentAmount.longValue());
        } else {
            // 새 IP 누적 금액 초과
            BigDecimal total = previousAmount.add(currentAmount);
            reason = String.format("새 IP 누적 금액 초과 (기존: %,d원 + 현재: %,d원 = %,d원, 한도: %,d원, 등록 후 %d시간)",
                    previousAmount.longValue(), currentAmount.longValue(), 
                    total.longValue(), limit.longValue(), hoursPassed);
        }

        // 이벤트 발행
        HijackDetectedEvent event = new HijackDetectedEvent(
                userId,
                userEmail,
                existingIps,
                currentIp,
                currentAmount,
                reason
        );
        log.info("========== 이벤트 발행 시작 ==========");
        log.info("발행할 이벤트: userId={}, email={}", userId, userEmail);
        applicationEventPublisher.publishEvent(event);
        log.info("========== 이벤트 발행 완료 ==========");
    }
}
