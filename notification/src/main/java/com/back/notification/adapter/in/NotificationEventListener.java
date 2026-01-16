package com.back.notification.adapter.in;

import com.back.common.Settlement.event.SettlementCompletedEvent;
import com.back.common.market.event.BiddingFailedEvent;
import com.back.common.market.event.PriceDroppedEvent;
import com.back.common.market.event.PurchaseCanceledEvent;
import com.back.common.market.event.BiddingCompletedEvent;
import com.back.common.product.event.InspectionCompletedEvent;
import com.back.notification.app.NotificationFacade;
import com.back.notification.domain.enums.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationFacade notificationFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(BiddingCompletedEvent event) {
        notificationFacade.notify(Type.BID_COMPLETED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PurchaseCanceledEvent event) {
        notificationFacade.notify(Type.PURCHASE_CANCELED, event);
    }

    // 30일 초과되어 입찰에 실패
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(BiddingFailedEvent event) {
        notificationFacade.notify(Type.BID_FAILED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(InspectionCompletedEvent event) {
        notificationFacade.notify(Type.INSPECTION_COMPLETED, event);
    }

    // 정산 완료
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(SettlementCompletedEvent event) {
        notificationFacade.notify(Type.SETTLEMENT_COMPLETED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PriceDroppedEvent event) {
        notificationFacade.notify(Type.PRICE_DROPPED, event);
    }
}
