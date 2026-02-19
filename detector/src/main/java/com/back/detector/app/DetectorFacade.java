package com.back.detector.app;

import com.back.detector.app.usecase.CrawlingLogUseCase;
import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.exception.CrawlingDetectedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Aspect
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorFacade {
    private final BidSpamDetector bidSpamDetector;
    private final CrawlingDetector crawlingDetector;
    private final HijackDetector hijackDetector;
    private final CrawlingLogUseCase crawlingLogUseCase;

    @Transactional
    public void detectBidSpam(Long userId) {
        bidSpamDetector.checkBidSpam(userId);
    }

    @Transactional
    public void detectCrawling(String ip) {
        CrawlingBanLevel banLevel = crawlingDetector.checkCrawling(ip);
        if (banLevel != null) {
            crawlingLogUseCase.save(ip, banLevel);
            throw new CrawlingDetectedException();
        }
    }

    /**
     * 계정 탈취 감지 (마켓 컨트롤러에서 직접 호출)
     * @param userId 사용자 ID
     * @param userEmail 사용자 이메일
     * @param ip 현재 요청 IP 주소
     * @param transactionAmount 거래 금액
     */
    @Transactional
    public void detectHijack(Long userId, String userEmail, String ip, BigDecimal transactionAmount) {
        hijackDetector.checkHijack(userId, userEmail, ip, transactionAmount);
    }

}
