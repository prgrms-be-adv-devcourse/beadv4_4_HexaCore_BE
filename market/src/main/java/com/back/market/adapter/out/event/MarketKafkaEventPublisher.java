package com.back.market.adapter.out.event;

import com.back.common.code.FailureCode;
import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.event.KafkaPayload;
import com.back.common.exception.CustomException;
import com.back.market.event.OrderCreatedEvent;
import com.back.market.event.SellBiddingCreatedEvent;
import com.back.market.event.payload.OrderCompletedPayload;
import com.back.market.event.OrderCompletedEvent;
import com.back.market.event.payload.OrderCreatedPayload;
import com.back.market.event.payload.SellBiddingCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketKafkaEventPublisher {

    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${custom.kafka.topic.market-order-completed}")
    private String orderCompletedTopic;
    @Value("${custom.kafka.topic.market-bidding-sell-created}")
    private String sellBiddingCreatedTopic;
    @Value("${custom.kafka.topic.market-order-created}")
    private String orderCreatedTopic;

    // 공통 실행 템플릿 메서드
    private <T extends KafkaPayload> void publishWithErrorHandler(String topic, Envelope<T> envelope, Runnable publishTask) {
        try {
            publishTask.run();
            log.info("[MarketKafkaEventPublisher] 카프카 이벤트 발행 완료. | Topic: {}, Data: {}, eventId: {}", topic, envelope.payload(), envelope.header().eventId());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MarketKafkaEventPublisher] 카프카 발행 중 시스템 오류 발생. | Topic: {}, Data: {}, , eventId: {}, Error: {}", topic, envelope.payload(), envelope.header().eventId(), e.getMessage());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 구매 확정 이벤트
    public void sendOrderConfirmed(OrderCompletedEvent event) {
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

        publishWithErrorHandler(orderCompletedTopic, envelope, () -> kafkaEventPublisher.publish(orderCompletedTopic, envelope));
    }

    // 판매 입찰 생성 이벤트
    public void sendSellBiddingCreated(SellBiddingCreatedEvent event) {
        //검증 및 예외처리
        if (event.biddingId() == null || event.sellerUserId() == null) {
            log.error("[MarketKafkaEventPublisher] 필수 데이터 누락: biddingId={}, sellerId={}", event.biddingId(), event.sellerUserId());
            throw new CustomException(FailureCode.MISSING_REQUIRED_FIELD);
        }

        // 페이로드 생성 및 전송
        SellBiddingCreatedPayload payload = SellBiddingCreatedPayload.of(
                event.biddingId(),
                event.sellerUserId(),
                event.productId(),
                event.productName(),
                event.productNumber(),
                event.productOption(),
                event.brandName(),
                event.categoryName(),
                event.thumbnailImage(),
                event.currentPrice()
        );

        Envelope<SellBiddingCreatedPayload> envelope = Envelope.of(sellBiddingCreatedTopic, payload);

        publishWithErrorHandler(sellBiddingCreatedTopic, envelope, () -> kafkaEventPublisher.publish(sellBiddingCreatedTopic, envelope));
    }

    // 주문 생성 이벤트
    public void sendOrderCreated(OrderCreatedEvent event) {

        // 검증
        if (event.biddingId() == null || event.buyerUserId() == null || event.sellerUserId() == null || event.productName() == null) {

            log.error("[MarketKafkaEventPublisher] 주문 생성 이벤트 필수 데이터 누락: orderId={}, buyer={}, seller={}, product={}",
                    event.biddingId(), event.buyerUserId(), event.sellerUserId(), event.productName());
            throw new CustomException(FailureCode.MISSING_REQUIRED_FIELD);
        }

        // 페이로드 생성 및 전송
        OrderCreatedPayload payload = OrderCreatedPayload.of(
                event.biddingId(),
                event.buyerUserId(),
                event.sellerUserId(),
                event.productId(),
                event.productName(),
                event.productSize(),
                event.thumbnailImage(),
                event.brandName(),
                event.price(),
                event.biddingPosition()
        );

        Envelope<OrderCreatedPayload> envelope = Envelope.of(orderCreatedTopic, payload);

        publishWithErrorHandler(orderCreatedTopic, envelope, () -> kafkaEventPublisher.publish(orderCreatedTopic, envelope));
    }
}
