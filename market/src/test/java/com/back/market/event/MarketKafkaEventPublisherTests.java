package com.back.market.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.exception.CustomException;
import com.back.market.adapter.out.event.MarketKafkaEventPublisher;
import com.back.market.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MarketKafkaEventPublisherTests {
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private MarketKafkaEventPublisher marketKafkaEventPublisher;

    @BeforeEach
    void setUp() {
        // yml에 정의하신 "market.order.completed" 값을 테스트 객체의 private 필드에 강제로 주입합니다.
        ReflectionTestUtils.setField(marketKafkaEventPublisher, "orderCompletedTopic", "market.order.completed");
    }

    @Test
    @DisplayName("성공: 올바른 데이터가 주어지면 카프카로 Envelope 메시지를 발행한다.")
    void sendOrderConfirmed_success() {
        OrderCompletedEvent event = new OrderCompletedEvent(1L, 1L, 100L, 500L, "판매자A", new BigDecimal("50000.00"), OrderStatus.COMPLETED, LocalDateTime.now());
        marketKafkaEventPublisher.sendOrderConfirmed(event);
        verify(kafkaEventPublisher).publish(eq("market.order.completed"), any(Envelope.class));
    }

    @Test
    @DisplayName("실패: 필수 값(orderId)이 누락되면 MISSING_REQUIRED_FIELD 예외가 발생한다")
    void sendOrderConfirmed_Fail_MissingField() {
        // Given: orderId가 null인 비정상 이벤트
        OrderCompletedEvent invalidEvent = new OrderCompletedEvent(
                null, 100L, 500L, 600L, "판매자A", new BigDecimal("50000.00"), OrderStatus.COMPLETED, LocalDateTime.now()
        );

        // When & Then: 예외 발생 여부 검증
        assertThrows(CustomException.class, () -> {
            marketKafkaEventPublisher.sendOrderConfirmed(invalidEvent);
        });
    }
}
