package com.back.market.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.market.event.payload.OrderCompletedPayload;
import com.back.market.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketKafkaEventPublisher {

    @Value("${custom.kafka.topic.market-order-completed}")
    private String orderCompletedTopic;
    private final KafkaEventPublisher kafkaEventPublisher;

    public void sendOrderConfirmed(OrderCompletedEvent event) {
        OrderCompletedPayload payload = OrderCompletedPayload.of(
                event.orderId(),
                event.productId(),
                event.buyerId(),
                event.sellerId(),
                event.sellerName(),
                event.price(),
                event.orderStatus(),
                event.confirmedAt()
        );

        Envelope<OrderCompletedPayload> envelope = Envelope.of(orderCompletedTopic, payload);

        kafkaEventPublisher.publish(orderCompletedTopic, envelope);

        log.info("[MarketKafkaEventPublisher] OrderCompletedEvent 생성, 카프카로 발행됨 eventId={}, occurredAt={}, topic={}, orderId={}",
                envelope.header().eventId(), envelope.header().occurrenceAt(), orderCompletedTopic, event.orderId());
    }

}
