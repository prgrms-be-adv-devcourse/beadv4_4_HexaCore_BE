package com.back.product.app;

import com.back.product.app.usecase.*;
import com.back.product.dto.BrandDto;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.OptionDto;
import com.back.product.dto.ProductInfoDto;
import com.back.product.global.event.ProductCreationCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext // 테스트 간 컨텍스트를 분리하여 Kafka 브로커 충돌 방지
@EmbeddedKafka(partitions = 1, topics = { "${custom.kafka.topic.product-created:product-created}" })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "custom.kafka.topic.product-created=" + ProductSyncEventPublishTest.TEST_TOPIC,
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
class ProductSyncEventPublishTest {
    public static final String TEST_TOPIC = "test-product-created";

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // 최종 도착지인 UseCase만 Mocking하여 호출 여부를 검증
    @MockitoBean
    private ProductDocumentUseCase productDocumentUseCase;

    @Test
    @DisplayName("ProductCreationCompletedEvent 발행 시, Kafka를 거쳐 최종적으로 ES 동기화 로직이 호출된다")
    void testEventPublishAndSubscribeFlow() {
        // --- 1. Arrange (테스트 준비) ---
        // 테스트용 이벤트 객체 생성
        ProductInfoDto productInfoDto = new ProductInfoDto(
                1L,
                new BrandDto(1L, "Brand", "logo.png"),
                new CategoryDto(1L, "Category", "image.png"),
                "Test Product",
                "P001",
                BigDecimal.TEN,
                LocalDateTime.now()
        );

        List<OptionDto> optionDtos = List.of(new OptionDto(
                new OptionDto.GroupDto(1L, "Color"),
                List.of(new OptionDto.ValueDto(1L, "Red"))
        ));

        String thumbnailUrl = "http://test.com/image.jpg";
        ProductCreationCompletedEvent event = new ProductCreationCompletedEvent(productInfoDto, optionDtos, thumbnailUrl);


        // --- 2. Act (이벤트 발행) --- : 메서드 내부에서 명시적으로 트랜잭션을 실행하고 커밋시킴
        // Facade의 private 메소드 대신, ApplicationEventPublisher를 통해 직접 이벤트를 발행하여 흐름을 시작
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null; // 여기서 트랜잭션이 끝나면서 실제 COMMIT 발생 -> Kafka 전송 시작
        });


        // --- 3. Assert (결과 검증) ---
        // Kafka 리스너는 비동기적으로 동작하므로, Awaitility를 사용해 최대 5초간 대기하며 검증을 시도
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // ProductDocumentUseCase의 syncProduct 메소드가 1번 호출되었는지 검증
            ArgumentCaptor<ProductInfoDto> infoDtoCaptor = ArgumentCaptor.forClass(ProductInfoDto.class);
            ArgumentCaptor<List<OptionDto>> optionsCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

            verify(productDocumentUseCase, times(1)).syncProduct(
                    infoDtoCaptor.capture(),
                    optionsCaptor.capture(),
                    urlCaptor.capture()
            );

            // 최종적으로 전달된 데이터가 발행했던 데이터와 일치하는지 검증
            assertThat(infoDtoCaptor.getValue().productInfoId()).isEqualTo(1L);
            assertThat(infoDtoCaptor.getValue().name()).isEqualTo("Test Product");
            assertThat(optionsCaptor.getValue()).hasSize(1);
            assertThat(urlCaptor.getValue()).isEqualTo("http://test.com/image.jpg");
        });
    }
}