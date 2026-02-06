package com.back.detector.app;

import com.back.common.util.IpAddressExtractor;
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

    @Around("@annotation(com.back.detector.annotation.CheckHijack)")
    public Object detectHijack(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = getCurrentUserId();
        String ip = getCurrentIp();

        BigDecimal transactionAmount = extractTransactionAmount(joinPoint.getArgs());
        
        hijackDetector.checkHijack(userId, ip, transactionAmount);
        return joinPoint.proceed();
    }

    /**
     * 메서드 파라미터에서 거래금액 추출
     */
    private BigDecimal extractTransactionAmount(Object[] args) {
        for (Object arg : args) {
            // BigDecimal 타입 직접 체크
            if (arg instanceof BigDecimal) {
                return (BigDecimal) arg;
            }
            
            // BiddingRequestDto에서 가격 추출
            if (arg != null && arg.getClass().getSimpleName().equals("BiddingRequestDto")) {
                try {
                    // record의 price() 메서드 호출
                    var method = arg.getClass().getMethod("price");
                    Object price = method.invoke(arg);
                    if (price instanceof BigDecimal) {
                        return (BigDecimal) price;
                    }
                } catch (Exception e) {
                    log.warn("BiddingRequestDto에서 가격 추출 실패", e);
                }
            }
        }
        
        // 금액을 찾지 못하면 예외 발생
        log.error("거래 금액을 찾을 수 없습니다. args: {}", (Object) args);
        throw new IllegalStateException("거래 금액을 찾을 수 없습니다");
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
