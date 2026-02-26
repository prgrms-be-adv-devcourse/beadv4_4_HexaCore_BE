package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.domain.Bidding;
import com.back.market.domain.enums.BiddingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiddingStatusService {
    private final BiddingRepository biddingRepository;

    /**
     * 새로운 트랜잭션에서 bidding 상태를 변경하는 메서드
     * @param biddingId 변경할 bidding의 ID
     * @param status 변경할 상태
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStatusInNewTx(Long biddingId, BiddingStatus status) {
        log.info("[BiddingStatusService] markStatusInNewTx 시작 - biddingId={}, newStatus={}", biddingId, status);
        try {
            Bidding bidding = biddingRepository.findById(biddingId)
                    .orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

            bidding.changeStatus(status);
            biddingRepository.save(bidding);
            log.info("[BiddingStatusService] markStatusInNewTx 커밋됨 - biddingId={}, newStatus={}", biddingId, status);
        } catch (Exception e) {
            log.error("[BiddingStatusService] markStatusInNewTx 실패 - biddingId={}, newStatus={}, error={}", biddingId, status, e.getMessage(), e);
            throw e;
        }
    }
}
