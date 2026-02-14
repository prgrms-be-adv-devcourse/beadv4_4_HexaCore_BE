package com.back.market.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.market.event.OrderCompletedEvent;
import com.back.market.app.MarketFacade;
import com.back.market.app.usecase.CompleteOrderUseCase;
import com.back.market.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderCompleteEventTest {
    @Mock
    private CompleteOrderUseCase completeOrderUseCase;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private MarketFacade marketFacade;

    @BeforeEach
    void setUp() {
        // marketFacade 객체의 "orderCompletedTopic" 필드에 "order-confirmed-topic" 값을 직접 주입합니다.
        ReflectionTestUtils.setField(marketFacade, "orderCompletedTopic", "market.order.completed");
    }

    @Test
    @DisplayName("구매 확정 성공 시 정산 이벤트가 발행되어야 한다")
    void completeOrder_success_and_publish_event() {
        // given
        Long userId = 1L;
        Long orderId = 100L;
        // Mock Order 객체 생성 (빌더 사용)
        Order mockOrder = createMockOrder(orderId, userId);

        given(completeOrderUseCase.completeOrder(userId, orderId)).willReturn(mockOrder);

        // when
        marketFacade.completeOrder(userId, orderId);

        // then
        // 1. 유스케이스가 정상 호출되었는지 확인
        verify(completeOrderUseCase, times(1)).completeOrder(userId, orderId);

        // 2. 카프카 퍼블리셔가 '특정 토픽'과 '이벤트'를 가지고 호출되었는지 확인
        verify(kafkaEventPublisher, times(1)).publish(anyString(), any(Envelope.class));
    }
    /**
     * 테스트용 가짜 Order 객체를 생성하는 헬퍼 메서드
     */
    private Order createMockOrder(Long orderId, Long buyerId) {
        // Order 엔티티 내부의 연관관계(Bidding)까지 가짜로 만들어줍니다.
        return Order.builder()
                .id(orderId)
                .price(new java.math.BigDecimal("50000"))
                .buyBidding(com.back.market.domain.Bidding.builder()
                        .marketUser(com.back.market.domain.MarketUser.builder().id(buyerId).build())
                        .build())
                .sellBidding(com.back.market.domain.Bidding.builder()
                        .marketUser(com.back.market.domain.MarketUser.builder().id(999L).name("판매자").build())
                        .marketProduct(com.back.market.domain.MarketProduct.builder().id(500L).build())
                        .build())
                .build();
    }
}
