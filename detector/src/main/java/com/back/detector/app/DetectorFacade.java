package com.back.detector.app;

import com.back.detector.exception.BidSpamException;
import com.back.detector.exception.CrawlingDetectedException;
import com.back.detector.exception.HijackDetectedException;
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
    private final HijackDetector hijackDetector;

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

    @Around("@annotation(com.back.detector.annotation.CheckHijack)")
    public Object detectHijack(ProceedingJoinPoint joinPoint) {
        try {
            Long userId = getCurrentUserId();
            String ip = getCurrentIp();

            Long transactionAmount = extractTransactionAmount(joinPoint.getArgs());
            
            hijackDetector.checkHijack(userId, ip, transactionAmount);
            return joinPoint.proceed();
        } catch (HijackDetectedException e) {
            log.warn("계정 탈취 감지 - 사용자ID: {}, IP: {}", getCurrentUserIdSafe(), getCurrentIpSafe());
            throw e;
        } catch (Throwable e) {
            log.error("메서드 실행 중 오류 발생", e);
            throw new RuntimeException("거래 처리 실패", e);
        }
    }

    /**
     * 메서드 파라미터에서 거래금액 추출
     */
    private Long extractTransactionAmount(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return 0L; // 금액을 찾지 못하면 0원으로 처리 (검사 통과)
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
