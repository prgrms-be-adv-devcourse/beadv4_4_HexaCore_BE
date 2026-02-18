package com.back.product.app;

import com.back.product.adapter.out.document.ProductDocumentRepository;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.dto.model.BrandDto;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@EnableAspectJAutoProxy(proxyTargetClass = true) // CGLIB 프록시 사용
class ProductSyncEventPublishTest {

    @MockitoBean
    private ProductDocumentRepository productDocumentRepository;

    // 이 테스트는 Kafka를 통해 이벤트가 최종 목적지까지 잘 도달하는지 확인하는 종단 테스트(E2E)에 가깝습니다.
    // 따라서 최종 consumer의 역할인 ProductDocumentUseCase를 Mocking합니다.
    @MockitoBean
    private ProductDocumentUseCase productDocumentUseCase;

    // 실제 Spring의 이벤트 발행기를 사용하여 트랜잭션과 함께 이벤트를 발행합니다.
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    // 트랜잭션 경계를 프로그래밍 방식으로 제어하기 위해 사용합니다.
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Nested
    @DisplayName("ProductCreatedEventTest 발행 및 구독 테스트")
    class ProductCreatedEventTest {
        @Test
        @DisplayName("ProductCreationCompletedEvent 발행 시, Kafka를 거쳐 최종적으로 ES 동기화 로직이 호출된다")
        void testEventPublishAndSubscribeFlow() {
            // --- 1. Arrange (테스트 준비) ---
            String eventId = UUID.randomUUID().toString();

            ProductInfoDto productInfoDto = new ProductInfoDto(
                    1L,
                    new BrandDto(1L, "Brand", "logo.png"),
                    new CategoryDto(1L, "Category", "image.png"),
                    "TestProduct",
                    "P001",
                    BigDecimal.TEN,
                    LocalDateTime.now()
            );

            List<OptionDto> optionDtos = List.of(new OptionDto(
                    new OptionDto.GroupDto(1L, "color"),
                    List.of(new OptionDto.ValueDto(1L, "red"))
            ));

            String thumbnailUrl = "http://test.com/image.jpg";
            ProductCreationCompletedEvent event = new ProductCreationCompletedEvent(eventId, productInfoDto, optionDtos, thumbnailUrl);

            // --- 2. Act (이벤트 발행) ---
            transactionTemplate.execute(status -> {
                applicationEventPublisher.publishEvent(event);
                return null; // COMMIT 발생 -> 아웃박스 저장 -> AFTER_COMMIT 리스너 실행 -> Kafka 전송
            });

            // --- 3. Assert (결과 검증) ---
            // 비동기 처리를 기다리기 위해 Awaitility 사용
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                ArgumentCaptor<ProductInfoDto> infoDtoCaptor = ArgumentCaptor.forClass(ProductInfoDto.class);
                ArgumentCaptor<List<OptionDto>> optionsCaptor = ArgumentCaptor.forClass(List.class);
                ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

                // 최종 consumer인 productDocumentUseCase.syncProduct가 호출되었는지 검증
                verify(productDocumentUseCase, times(1)).syncProduct(
                        infoDtoCaptor.capture(),
                        optionsCaptor.capture(),
                        urlCaptor.capture()
                );

                assertThat(infoDtoCaptor.getValue().productInfoId()).isEqualTo(1L);
                assertThat(infoDtoCaptor.getValue().name()).isEqualTo("TestProduct");
                assertThat(optionsCaptor.getValue()).hasSize(1);
                assertThat(urlCaptor.getValue()).isEqualTo("http://test.com/image.jpg");

                verifyNoMoreInteractions(productDocumentUseCase);
            });
        }
    }

    @Nested
    @DisplayName("ProductUpdatedEventTest 발행 및 구독 테스트")
    class ProductUpdatedEventTest {
        @Test
        @DisplayName("ProductUpdateCompletedEvent 발행 시, Kafka를 거쳐 최종적으로 ES 동기화 로직이 호출된다")
        void testProductUpdateEventFlow() {
            // --- 1. Arrange (테스트 준비) ---
            String eventId = UUID.randomUUID().toString();

            ProductInfoDto productInfoDto = new ProductInfoDto(
                    2L,
                    new BrandDto(1L, "UpdatedBrand", "logo.png"),
                    new CategoryDto(1L, "UpdatedCategory", "image.png"),
                    "UpdatedProduct",
                    "P002",
                    BigDecimal.valueOf(20),
                    LocalDateTime.now().plusHours(1)
            );

            List<OptionDto> optionDtos = List.of(new OptionDto(
                    new OptionDto.GroupDto(2L, "size"),
                    List.of(new OptionDto.ValueDto(2L, "large"))
            ));

            String thumbnailUrl = "http://test.com/updated_image.jpg";
            ProductUpdateCompletedEvent event = new ProductUpdateCompletedEvent(eventId, productInfoDto, optionDtos, thumbnailUrl);

            // --- 2. Act (이벤트 발행) ---
            transactionTemplate.execute(status -> {
                applicationEventPublisher.publishEvent(event);
                return null;
            });

            // --- 3. Assert (결과 검증) ---
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                ArgumentCaptor<ProductInfoDto> infoDtoCaptor = ArgumentCaptor.forClass(ProductInfoDto.class);
                ArgumentCaptor<List<OptionDto>> optionsCaptor = ArgumentCaptor.forClass(List.class);
                ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

                verify(productDocumentUseCase, times(1)).syncProduct(
                        infoDtoCaptor.capture(),
                        optionsCaptor.capture(),
                        urlCaptor.capture()
                );

                assertThat(infoDtoCaptor.getValue().productInfoId()).isEqualTo(2L);
                assertThat(infoDtoCaptor.getValue().name()).isEqualTo("UpdatedProduct");
                assertThat(optionsCaptor.getValue()).hasSize(1);
                assertThat(urlCaptor.getValue()).isEqualTo("http://test.com/updated_image.jpg");

                verifyNoMoreInteractions(productDocumentUseCase);
            });
        }
    }

    @Nested
    @DisplayName("ProductDeletedEventTest 발행 및 구독 테스트")
    class ProductDeletedEventTest {
        @Test
        @DisplayName("ProductDeletionCompletedEvent 발행 시, Kafka를 거쳐 최종적으로 ES 삭제 로직이 호출된다")
        void testProductDeletionEventFlow() {
            // --- 1. Arrange (테스트 준비) ---
            String eventId = UUID.randomUUID().toString();
            Long productInfoIdToDelete = 3L;
            ProductDeletionCompletedEvent event = new ProductDeletionCompletedEvent(eventId, productInfoIdToDelete);

            // --- 2. Act (이벤트 발행) ---
            transactionTemplate.execute(status -> {
                applicationEventPublisher.publishEvent(event);
                return null;
            });

            // --- 3. Assert (결과 검증) ---
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

                verify(productDocumentUseCase, times(1)).deleteProduct(idCaptor.capture());

                assertThat(idCaptor.getValue()).isEqualTo(productInfoIdToDelete);

                verifyNoMoreInteractions(productDocumentUseCase);
            });
        }
    }
}