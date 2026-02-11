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

    @Around("@annotation(com.back.detector.annotation.CheckBidSpam)")
    public Object detectBidSpam(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = getCurrentUserId();
        bidSpamDetector.checkBidSpam(userId);
        return joinPoint.proceed();
    }

    @Around("@annotation(com.back.detector.annotation.CheckCrawling)")
    public Object detectCrawling(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getCurrentIp();
        crawlingDetector.checkCrawling(ip);
        return joinPoint.proceed();
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

    private Long getCurrentUserId() {
        return SecurityHelper.getCurrentUserId();
    }

    private String getCurrentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("HTTP 요청 컨텍스트가 없습니다");
        }
        HttpServletRequest request = attributes.getRequest();
        return IpAddressExtractor.extractClientIp(request);
    }

    /**
     * 예외 로깅용 : getCurrentIp()가 실패해도 로그는 남기려는 용도
     */
    private String getCurrentIpSafe() {
        try {
            return getCurrentIp();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 예외 로깅용 : getCurrentUserId()가 실패해도 로그는 남기려는 용도
     */
    private Long getCurrentUserIdSafe() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
