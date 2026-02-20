package com.back.settlement.app.event.handler;

import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementOutboxListener {
    private final SettlementOutboxEventRecorder eventRecorder;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void recordMessageHandle(SettlementInternalCompletedEvent event) {
        eventRecorder.saveToOutbox(event);
    }
}
