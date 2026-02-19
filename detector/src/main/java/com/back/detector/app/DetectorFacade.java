package com.back.detector.app;

import com.back.common.util.IpAddressExtractor;
import com.back.security.util.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;

@Aspect
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorFacade {
    private final BidSpamDetector bidSpamDetector;
    private final CrawlingDetector crawlingDetector;
    private final HijackDetector hijackDetector;

    @Transactional
    public void detectBidSpam(Long userId) {
        bidSpamDetector.checkBidSpam(userId);
    }

    @Transactional
    public void detectCrawling(String ip) {
        crawlingDetector.checkCrawling(ip);
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
