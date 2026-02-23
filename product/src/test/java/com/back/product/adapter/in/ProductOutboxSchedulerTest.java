package com.back.product.adapter.in;

import com.back.product.adapter.in.scheduler.ProductOutboxScheduler;
import com.back.product.adapter.out.event.BrandKafkaEventPublisher;
import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.app.facade.ProductOutboxFacade;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOutboxScheduler 단위 테스트")
class ProductOutboxSchedulerTest {

    @InjectMocks
    private ProductOutboxScheduler scheduler;

    @Mock
    private ProductOutboxFacade productOutboxFacade;

    @Mock
    private ProductOutboxUseCase productOutboxUseCase;

    @Mock
    private ProductKafkaEventPublisher productPublisher;

    @Mock
    private BrandKafkaEventPublisher brandPublisher;

    @BeforeEach
    void setUp() {
        // @Value 필드 수동 주입
        ReflectionTestUtils.setField(scheduler, "SIZE", 10L);
        ReflectionTestUtils.setField(scheduler, "MAX_PER_RUN", 20L);

        // @PostConstruct 수동 호출하여 publisherMap 초기화
        scheduler.init();
    }

    @Test
    @DisplayName("재처리 대상을 성공적으로 조회하여 Facade로 발행을 트리거한다")
    void retryPendingEvents_Success() {
        // given
        String eventId = UUID.randomUUID().toString();
        ProductOutboxEvent event = ProductOutboxEvent.builder()
                .eventId(eventId)
                .eventType(ProductCreationCompletedEvent.class.getSimpleName())
                .build();

        given(productOutboxUseCase.findEventIds(10L))
                .willReturn(List.of(event)) // 첫 루프에서 1건 반환
                .willReturn(List.of());     // 두 번째 루프에서 빈 리스트 반환 (종료 조건)

        // when
        scheduler.retryPendingEvents();

        // then
        verify(productOutboxFacade, times(1)).publish(eq(eventId), any());
        verify(productOutboxUseCase, atLeastOnce()).findEventIds(10L);
    }

    @Test
    @DisplayName("지원하지 않는 이벤트 타입이 들어오면 실패 카운트를 올리고 다음으로 진행한다")
    void retryPendingEvents_UnknownEventType() {
        // given
        ProductOutboxEvent unknownEvent = ProductOutboxEvent.builder()
                .eventId("unknown-id")
                .eventType("UnknownEvent")
                .build();

        given(productOutboxUseCase.findEventIds(10L))
                .willReturn(List.of(unknownEvent))
                .willReturn(List.of());

        // when
        scheduler.retryPendingEvents();

        // then
        // Facade.publish가 호출되지 않아야 함 (Map 룩업 실패 시 return false)
        verify(productOutboxFacade, never()).publish(any(), any());
    }

    @Test
    @DisplayName("Facade 호출 중 예외가 발생해도 스케줄러 전체가 중단되지 않고 다음 이벤트를 처리한다")
    void retryPendingEvents_ContinuesOnException() {
        // given
        ProductOutboxEvent event1 = ProductOutboxEvent.builder()
                .eventId("id-1")
                .eventType(ProductCreationCompletedEvent.class.getSimpleName())
                .build();
        ProductOutboxEvent event2 = ProductOutboxEvent.builder()
                .eventId("id-2")
                .eventType(ProductCreationCompletedEvent.class.getSimpleName())
                .build();

        given(productOutboxUseCase.findEventIds(10L))
                .willReturn(List.of(event1, event2))
                .willReturn(List.of());

        // 첫 번째 이벤트 처리 시 예외 발생시키기
        doThrow(new RuntimeException("Lock Error"))
                .when(productOutboxFacade).publish(eq("id-1"), any());

        // when
        scheduler.retryPendingEvents();

        // then
        // id-1은 실패했지만 id-2는 여전히 호출되어야 함
        verify(productOutboxFacade).publish(eq("id-1"), any());
        verify(productOutboxFacade).publish(eq("id-2"), any());
    }

    @Test
    @DisplayName("MAX_PER_RUN 제한에 도달하면 루프를 종료한다")
    void retryPendingEvents_RespectsMaxPerRun() {
        // given
        // SIZE는 10, MAX_PER_RUN은 20으로 설정됨
        ProductOutboxEvent event = ProductOutboxEvent.builder()
                .eventId("id")
                .eventType(ProductCreationCompletedEvent.class.getSimpleName())
                .build();

        List<ProductOutboxEvent> batch = List.of(event, event, event, event, event, event, event, event, event, event);

        // 계속해서 10개씩(SIZE) 반환하도록 설정
        given(productOutboxUseCase.findEventIds(10L)).willReturn(batch);

        // when
        scheduler.retryPendingEvents();

        // then
        // 총 20개까지만 처리하고 멈췄는지 검증 (while 루프가 2번만 돌아야 함)
        verify(productOutboxUseCase, times(2)).findEventIds(10L);
        verify(productOutboxFacade, times(20)).publish(any(), any());
    }
}