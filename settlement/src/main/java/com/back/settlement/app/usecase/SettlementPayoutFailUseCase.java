package com.back.settlement.app.usecase;

import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementPayoutFailUseCase {
    private final SettlementRepository settlementRepository;

    @Transactional
    public void markAsFailed(Long settlementId, String reason) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElse(null);

        if (settlement == null) {
            log.warn("정산을 찾을 수 없습니다. 실패 처리 생략. settlementId={}", settlementId);
            return;
        }

        if (settlement.getStatus() == SettlementStatus.FAILED) {
            log.info("이미 실패 처리된 정산입니다. settlementId={}", settlementId);
            return;
        }

        settlement.fail(reason);
        settlementRepository.save(settlement);
        log.info("정산 지급 실패 처리 완료. settlementId={}, reason={}", settlementId, reason);
    }
}
