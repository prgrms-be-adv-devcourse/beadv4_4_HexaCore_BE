package com.back.market.adapter.out.event;

import com.back.common.code.FailureCode;
import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.exception.CustomException;
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
        try {
            // 1. 데이터 검증 및 예외 처리
            if(event.orderId() == null || event.sellerId() == null) {
                log.error("[MarketKafkaEventPublisher] 필수 데이터 누락: orderId={}, sellerId={}", event.orderId(), event.sellerId());
                throw new CustomException(FailureCode.MISSING_REQUIRED_FIELD);
            }

            // 2. 페이로드 생성 및 전송
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

            log.info("[MarketKafkaEventPublisher] 카프카 이벤트 발행 완료. | Topic: {} | OrderID: {} | Seller: {} | Price: {} | EventID: {}",
                    orderCompletedTopic,
                    event.orderId(),
                    event.sellerId(),
                    event.price(),
                    envelope.header().eventId());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            // 직렬화 오류나 네트워크 오류 등 예상치 못한 시스템 예외 처리
            log.error("[MarketKafkaEventPublisher] 카프카 발행 중 시스템 오류 발생: {}", e.getMessage());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }
    }

}
