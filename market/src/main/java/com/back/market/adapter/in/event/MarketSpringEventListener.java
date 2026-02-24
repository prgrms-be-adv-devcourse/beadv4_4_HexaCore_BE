package com.back.market.adapter.in.event;

import com.back.market.adapter.out.event.MarketKafkaEventPublisher;
import com.back.market.event.OrderCompletedEvent;
import com.back.market.event.OrderCreatedEvent;
import com.back.market.event.SellBiddingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketSpringEventListener {

    private final MarketKafkaEventPublisher marketKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompletedEvent(OrderCompletedEvent springEvent) {
        marketKafkaEventPublisher.sendOrderCompleted(springEvent);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSellBiddingCreatedEvent(SellBiddingCreatedEvent springEvent) {
        marketKafkaEventPublisher.sendSellBiddingCreated(springEvent);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent springEvent) {
        marketKafkaEventPublisher.sendOrderCreated(springEvent);
    }

}
