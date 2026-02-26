package com.back.market.adapter.out.detector;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.market.adapter.out.detector.dto.request.DetectHijackRequestDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDetectorAdapter {
    private final DetectorClient detectorClient;

    public void detectBidSpam(Long userId) {
        try {
            detectorClient.detectBidSpam(userId);
        } catch (FeignException.TooManyRequests e) {
            throw new CustomException(FailureCode.BID_SPAM_DETECTED);
        } catch (FeignException e) {
            log.error("[DetectorAdapter] Detector 서비스 호출 실패: userId={}", userId, e);
            // 탐지 서비스 장애 시 입찰은 허용
        }
    }

    public void detectHijack(Long userId, String userEmail, String ip, BigDecimal transactionAmount) {
        try {
            detectorClient.detectHijack(new DetectHijackRequestDto(userId, userEmail, ip, transactionAmount));
        } catch (FeignException e) {
            log.error("[DetectorAdapter] Detector 서비스 hijack 호출 실패: userId={}", userId, e);
            // 탐지 서비스 장애 시 허용
        }
    }
}
