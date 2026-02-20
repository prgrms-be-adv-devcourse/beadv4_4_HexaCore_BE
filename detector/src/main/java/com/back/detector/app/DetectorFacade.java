package com.back.detector.app;

import com.back.detector.app.usecase.BidSpamLogUseCase;
import com.back.detector.app.usecase.CrawlingLogUseCase;
import com.back.detector.app.usecase.HijackLogUseCase;
import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.CrawlingDetectResult;
import com.back.detector.domain.HijackDetectResult;
import com.back.detector.dto.response.BidSpamLogResponse;
import com.back.detector.dto.response.CrawlingLogResponse;
import com.back.detector.dto.response.HijackLogResponse;
import com.back.detector.exception.BidSpamException;
import com.back.detector.exception.CrawlingDetectedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectorFacade {
    private final BidSpamDetector bidSpamDetector;
    private final CrawlingDetector crawlingDetector;
    private final HijackDetector hijackDetector;
    private final BidSpamLogUseCase bidSpamLogUseCase;
    private final CrawlingLogUseCase crawlingLogUseCase;
    private final HijackLogUseCase hijackLogUseCase;

    @Transactional
    public void detectBidSpam(Long userId) {
        BidSpamBanLevel banLevel = bidSpamDetector.checkBidSpam(userId);
        if (banLevel != null) {
            bidSpamLogUseCase.save(userId, banLevel);
            throw new BidSpamException();
        }
    }

    @Transactional
    public void detectCrawling(String ip) {
        CrawlingDetectResult result = crawlingDetector.checkCrawling(ip);
        if (result != null) {
            crawlingLogUseCase.save(ip, result.requestCount(), result.banLevel());
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
        HijackDetectResult result = hijackDetector.checkHijack(userId, userEmail, ip, transactionAmount);
        if (result != null) {
            hijackLogUseCase.save(result.userId(), result.userEmail(), result.currentIp(),
                    result.existingIps(), result.transactionAmount(), result.reason());
        }
    }

    public Page<BidSpamLogResponse> findLatestBidSpamLogs(int page, int size) {
        return bidSpamLogUseCase.findPageByCreatedAtDesc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<CrawlingLogResponse> findLatestCrawlingLogs(int page, int size) {
        return crawlingLogUseCase.findPageByCreatedAtDesc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<HijackLogResponse> findLatestHijackLogs(int page, int size) {
        return hijackLogUseCase.findPageByCreatedAtDesc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

}
