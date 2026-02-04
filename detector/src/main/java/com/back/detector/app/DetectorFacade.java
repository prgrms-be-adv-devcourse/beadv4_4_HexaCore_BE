package com.back.detector.app;

import com.back.detector.exception.BidSpamException;
import com.back.security.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

@Aspect
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorFacade {
    private final BidSpamDetector bidSpamDetector;

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

    private Long getCurrentUserId() {
        return SecurityHelper.getCurrentUserId();
    }

}
