package com.back.detector.app;

import com.back.detector.exception.BidSpamException;
import com.back.detector.exception.CrawlingDetectedException;
import com.back.security.util.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorFacade {
    private final BidSpamDetector bidSpamDetector;
    private final CrawlingDetector crawlingDetector;

    @Around("@annotation(com.back.detector.annotation.CheckBidSpam)")
    public Object detectBidSpam(ProceedingJoinPoint joinPoint) {
        try {
            Long userId = getCurrentUserId();
            bidSpamDetector.checkBidSpam(userId);
            return joinPoint.proceed();
        } catch (BidSpamException e) {
            log.warn("입찰 스팸 감지 - {}", e.getMessage());
            throw e;
        } catch (Throwable e) {
            log.error("메서드 실행 중 오류 발생", e);
            throw new RuntimeException("입찰 처리 실패", e);
        }
    }

    @Around("@annotation(com.back.detector.annotation.CheckCrawling)")
    public Object detectCrawling(ProceedingJoinPoint joinPoint) {
        try {
            String ip = getCurrentIp();
            crawlingDetector.checkCrawling(ip);
            return joinPoint.proceed();
        } catch (CrawlingDetectedException e) {
            log.warn("크롤링 봇 감지 - IP: {}", getCurrentIpSafe());
            throw e;
        } catch (Throwable e) {
            log.error("메서드 실행 중 오류 발생", e);
            throw new RuntimeException("상품 조회 처리 실패", e);
        }
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

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 예외 로깅용 — getCurrentIp()가 실패해도 로그는 남기려는 용도
     */
    private String getCurrentIpSafe() {
        try {
            return getCurrentIp();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
