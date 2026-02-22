package com.back.product.app;

import com.back.common.event.EventName;
import com.back.common.event.KafkaEventPublisher;
import com.back.product.adapter.in.event.BrandSpringEventListener;
import com.back.product.adapter.in.event.ProductSpringEventListener;
import com.back.product.adapter.out.document.ProductDocumentRepository;
import com.back.product.adapter.out.event.BrandKafkaEventPublisher;
import com.back.product.adapter.out.event.ProductKafkaEventPublisher;
import com.back.product.adapter.out.persistence.EventConsumptionLogRepository;
import com.back.product.adapter.out.persistence.ProductOutboxEventRepository;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.dto.enums.EventConsumptionStatus;
import com.back.product.dto.enums.OutboxEventStatus;
import com.back.product.dto.model.BrandDto;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext // 테스트 간 컨텍스트를 분리하여 Kafka 브로커 충돌 방지
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer",

        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@EnableAspectJAutoProxy(proxyTargetClass = true) // CGLIB 프록시 사용
class ProductSyncEventPublishTest {

    @TestConfiguration
    static class TestKafkaConfig {
        @Bean
        public KafkaTemplate<String, EventName> eventNameKafkaTemplate(
                ProducerFactory<Object, Object> producerFactory) {
            // Spring Boot가 제공하는 기본 ProducerFactory를 주입받아
            // 코드에서 요구하는 <String, EventName> 타입의 템플릿을 생성합니다.
            return new KafkaTemplate(producerFactory);
        }
    }

    @MockitoBean
    private BrandSpringEventListener brandSpringEventListener;

    @MockitoBean
    private BrandKafkaEventPublisher brandKafkaEventPublisher;

    @MockitoBean
    private ProductDocumentRepository productDocumentRepository;

    @MockitoBean
    private ProductDocumentUseCase productDocumentUseCase;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ProductOutboxEventRepository productOutboxEventRepository;

    @Autowired
    private EventConsumptionLogRepository eventConsumptionLogRepository;

    @Test
    @DisplayName("상품 생성 시나리오: Spring Event 발행 -> Outbox 기록 -> Kafka 발행 -> Kafka 소비 -> 최종 동기화 완료")
    void productCreationFlowTest() {
        // given
        String eventId = UUID.randomUUID().toString();
        ProductInfoDto productInfoDto = createProductInfoDto(1L, "New Product", "NP001");
        List<OptionDto> optionDtos = createOptionDtos();
        String thumbnailUrl = "https://example.com/thumb.jpg";

        ProductCreationCompletedEvent event = ProductCreationCompletedEvent.builder()
                .eventId(eventId)
                .productInfoDto(productInfoDto)
                .optionDtos(optionDtos)
                .thumbnailUrl(thumbnailUrl)
                .build();

        // when
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null;
        });

        // then
        // 1. ProductDocumentUseCase.syncProduct가 호출되어야 함 (최종 목적지)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(productDocumentUseCase, atLeastOnce()).syncProduct(any(), any(), any());
        });

        // 2. Outbox 상태 확인: SUCCEEDED
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ProductOutboxEvent outboxEvent = productOutboxEventRepository.findByEventId(eventId).orElseThrow();
            assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SUCCEEDED);
        });

        // 5. EventConsumptionLog 상태 확인: SUCCEEDED
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var log = eventConsumptionLogRepository.findByEventId(eventId).orElseThrow();
            assertThat(log.getStatus()).isEqualTo(EventConsumptionStatus.SUCCEEDED);
        });

        verify(productDocumentUseCase, atLeastOnce()).syncProduct(any(), any(), any());
    }

    @Test
    @DisplayName("상품 수정 시나리오: Spring Event 발행 -> Outbox 기록 -> Kafka 발행 -> Kafka 소비 -> 최종 동기화 완료")
    void productUpdateFlowTest() {
        // given
        String eventId = UUID.randomUUID().toString();
        ProductInfoDto productInfoDto = createProductInfoDto(1L, "Updated Product", "UP001");
        List<OptionDto> optionDtos = createOptionDtos();
        String thumbnailUrl = "https://example.com/updated_thumb.jpg";

        ProductUpdateCompletedEvent event = ProductUpdateCompletedEvent.builder()
                .eventId(eventId)
                .productInfoDto(productInfoDto)
                .optionDtos(optionDtos)
                .thumbnailUrl(thumbnailUrl)
                .build();

        // when
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null;
        });

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(productDocumentUseCase, atLeastOnce()).syncProduct(any(), any(), any());
        });

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ProductOutboxEvent outboxEvent = productOutboxEventRepository.findByEventId(eventId).orElseThrow();
            assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SUCCEEDED);
        });

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var log = eventConsumptionLogRepository.findByEventId(eventId).orElseThrow();
            assertThat(log.getStatus()).isEqualTo(EventConsumptionStatus.SUCCEEDED);
        });

        verify(productDocumentUseCase, atLeastOnce()).syncProduct(any(), any(), any());
    }

    @Test
    @DisplayName("상품 삭제 시나리오: Spring Event 발행 -> Outbox 기록 -> Kafka 발행 -> Kafka 소비 -> 최종 삭제 완료")
    void productDeletionFlowTest() {
        // given
        String eventId = UUID.randomUUID().toString();
        Long productInfoId = 1L;

        ProductDeletionCompletedEvent event = ProductDeletionCompletedEvent.builder()
                .eventId(eventId)
                .productInfoId(productInfoId)
                .build();

        // when
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null;
        });

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(productDocumentUseCase, atLeastOnce()).deleteProduct(productInfoId);
        });

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ProductOutboxEvent outboxEvent = productOutboxEventRepository.findByEventId(eventId).orElseThrow();
            assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SUCCEEDED);
        });

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var log = eventConsumptionLogRepository.findByEventId(eventId).orElseThrow();
            assertThat(log.getStatus()).isEqualTo(EventConsumptionStatus.SUCCEEDED);
        });

        verify(productDocumentUseCase, atLeastOnce()).deleteProduct(any());
    }

    private ProductInfoDto createProductInfoDto(Long productInfoId, String name, String code) {
        return ProductInfoDto.builder()
                .productInfoId(productInfoId)
                .brand(createBrandDto())
                .category(createCategoryDto())
                .name(name)
                .code(code)
                .releasePrice(new BigDecimal("100000.00"))
                .releaseDate(LocalDateTime.now())
                .build();
    }

    private BrandDto createBrandDto() {
        return BrandDto.builder()
                .brandId(1L)
                .name("Nike")
                .imageUrl("https://example.com/nike.png")
                .build();
    }

    private CategoryDto createCategoryDto() {
        return CategoryDto.builder()
                .categoryId(1L)
                .name("shoes")
                .imageUrl("https://example.com/shoes.png")
                .build();
    }

    private List<OptionDto> createOptionDtos() {
        return List.of(
                OptionDto.builder()
                        .group(OptionDto.GroupDto.builder().id(1L).name("color").build())
                        .values(List.of(OptionDto.ValueDto.builder().id(1L).name("black").build()))
                        .build()
        );
    }
}
