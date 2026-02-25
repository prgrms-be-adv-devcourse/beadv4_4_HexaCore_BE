package com.back.market.event;

import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Bidding;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.OrderStatus;
import com.back.market.event.payload.PaymentCompletedPayload;
import com.back.market.event.payload.PaymentFailedPayload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class PaymentResultEventTests {

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    @Autowired
    private OrderRepository orderRepository; // 주문 확인용 레포지토리 주입

    @Value("${custom.kafka.topic.cash-payment-completed}")
    private String paymentCompletedTopic;

    @Value("${custom.kafka.topic.cash-payment-failed}")
    private String paymentFailedTopic;

    @Autowired
    private MarketSupport marketSupport;

    @Test
    @DisplayName("결제 완료 이벤트를 수신하면 주문 상태가 PAID로 변경되어야 한다")
    void testPaymentCompletedEvent() throws Exception {
        // 1. Given: 테스트용 데이터 준비 (미리 DB에 HOLD 상태인 주문을 하나 넣어둬야 합니다)
        Long testOrderId = 2001L;
        BigDecimal testAmount = new BigDecimal("210000");

        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                RelType.ORDER, testOrderId, testAmount
        );
        Envelope<PaymentCompletedPayload> envelope = Envelope.of("PaymentCompletedEvent", payload);

        // 2. When: 카프카로 이벤트 쏘기
        kafkaEventPublisher.publish(paymentCompletedTopic, envelope);
        Thread.sleep(3000); // 컨슈머가 처리할 시간 대기

        // 3. Then: DB에서 주문 상태가 PAID로 변경되었는지 확인
        Order updatedOrder = orderRepository.findById(testOrderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 상태가 HOLD -> PAID로 잘 바뀌었는지 확인
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("결제 실패 이벤트를 수신하면 주문 상태가 CANCELLED로 변경되어야 한다")
    void testPaymentFailedEvent() throws Exception {
        // 1. Given
        Long testOrderId = 2001L; // 2001번 주문을 타겟으로 함

        // 결제 실패는 금액 정보 없이 RelType과 ID만 전송
        PaymentFailedPayload payload = new PaymentFailedPayload(
                RelType.ORDER, testOrderId, "실패이유"
        );
        Envelope<PaymentFailedPayload> envelope = Envelope.of("PaymentFailedEvent", payload);

        // 이벤트 쏘기 전 진짜 HOLD 상태인지 확인
        Order beforeOrder = orderRepository.findById(testOrderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        log.info("[테스트 시작 전 DB 상태 확인] 현재 2001번 주문 상태: {}", beforeOrder.getOrderStatus());
        assertThat(beforeOrder.getOrderStatus()).isEqualTo(OrderStatus.HOLD);

        // 2. When (실행)
        kafkaEventPublisher.publish(paymentFailedTopic, envelope);

        // 컨슈머가 처리할 수 있도록 대기
        Thread.sleep(3000);

        // 3. Then (검증)
        // 주문 상태 검증 - 상태가 HOLD -> CANCELLED(취소)로 잘 바뀌었는지 확인
        Order updatedOrder = marketSupport.findOrderById(testOrderId);
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        // 구매 입찰 상태 검증 (CANCELLED_PAYMENT_FAILED)
        Bidding buyBid = marketSupport.findBiddingById(updatedOrder.getBuyBidding().getId());
        assertThat(buyBid.getStatus()).isEqualTo(BiddingStatus.CANCELLED_PAYMENT_FAILED);

        // 판매 입찰 상태 검증 (PROCESS)
        Bidding sellBid = marketSupport.findBiddingById(updatedOrder.getSellBidding().getId());
        assertThat(sellBid.getStatus()).isEqualTo(BiddingStatus.PROCESS);

        log.info("결제 실패 이벤트 수신 및 주문, 입찰 상태 변경 검증 완료");

    }
}