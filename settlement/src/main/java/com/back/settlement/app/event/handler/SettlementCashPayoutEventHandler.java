package com.back.settlement.app.event.handler;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.back.common.dto.settlement.SettlementPayoutRequest;
import com.back.settlement.adapter.out.feign.cash.CashClient;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementCashPayoutEventHandler {
    private final CashClient cashClient;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleSettlementCompleted(SettlementInternalCompletedEvent event) {
        log.debug("정산 완료 이벤트 수신. settlementId={}, sellerId={}", event.settlementId(), event.sellerId());

        SettlementPayoutRequest payoutRequest = new SettlementPayoutRequest(
                event.settlementId(),
                event.sellerId(),
                event.totalNetAmount(),
                event.completedAt()
        );
        cashClient.requestPayout(List.of(payoutRequest));
        log.info("캐시 지급 요청 완료. settlementId={}, amount={}", event.settlementId(), event.totalNetAmount());
    }
}
