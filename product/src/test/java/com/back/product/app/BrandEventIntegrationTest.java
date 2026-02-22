package com.back.product.app;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.product.adapter.out.document.ProductDocumentRepository;
import com.back.product.adapter.out.persistence.ProductOutboxEventRepository;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.dto.enums.OutboxEventStatus;
import com.back.product.dto.model.BrandDto;
import com.back.product.event.kafka.BrandCreatedPayload;
import com.back.product.event.kafka.BrandDeletedPayload;
import com.back.product.event.kafka.BrandUpdatedPayload;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.event.spring.BrandDeletionCompletedEvent;
import com.back.product.event.spring.BrandUpdateCompletedEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
class BrandEventIntegrationTest {

    @TestConfiguration
    static class TestKafkaConfig {
        @Bean
        public KafkaTemplate<String, Object> eventNameKafkaTemplate(
                ProducerFactory<Object, Object> producerFactory) {
            // Spring Boot가 제공하는 기본 ProducerFactory를 주입받아
            // 코드에서 요구하는 <String, EventName> 타입의 템플릿을 생성합니다.
            return new KafkaTemplate(producerFactory);
        }
    }

    @MockitoBean
    private ProductDocumentRepository productDocumentRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ProductOutboxEventRepository outboxRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    private Consumer<String, String> consumer;

    @Value("${custom.kafka.topic.product-brand-created}")
    private String createdTopic;

    @Value("${custom.kafka.topic.product-brand-updated}")
    private String updatedTopic;

    @Value("${custom.kafka.topic.product-brand-deleted}")
    private String deletedTopic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        // 테스트용 Kafka Consumer를 생성합니다.
        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new DefaultKafkaConsumerFactory<String, String>(props).createConsumer("test-group", "1");
        consumer.subscribe(List.of(createdTopic, updatedTopic, deletedTopic));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
        outboxRepository.deleteAll();
    }

    @Test
    @DisplayName("브랜드 생성 시 Kafka에 BrandCreated 이벤트가 발행되어야 한다")
    void createBrand_shouldPublishCreateEventToKafka() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        List<BrandDto> brandDtos = List.of(new BrandDto(1L, "New Kafka Brand", "https://thumbNail.png"));
        BrandCreationCompletedEvent event = new BrandCreationCompletedEvent(eventId, brandDtos);

        // When
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null;
        });

        // Then
        AtomicReference<ConsumerRecord<String, String>> recordRef = new AtomicReference<>();
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                return false;
            }
            recordRef.set(records.iterator().next());
            return true;
        });

        ConsumerRecord<String, String> record = recordRef.get();
        assertThat(record.topic()).isEqualTo(createdTopic);

        Envelope<BrandCreatedPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});
        BrandCreatedPayload payload = envelope.payload();

        assertThat(payload.brands()).hasSize(1);
        assertThat(payload.brands().get(0).brandId()).isEqualTo(1L);
        assertThat(payload.brands().get(0).name()).isEqualTo("New Kafka Brand");

        ProductOutboxEvent outboxEvent = outboxRepository.findByEventId(eventId).orElseThrow();
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SUCCEEDED);
    }

    @Test
    @DisplayName("브랜드 수정 시 Kafka에 BrandUpdated 이벤트가 발행되어야 한다")
    void modifyBrand_shouldPublishUpdateEventToKafka() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        BrandDto updatedBrandDto = new BrandDto(1L, "Updated Kafka Brand", "http://image2.png");
        BrandUpdateCompletedEvent event = new BrandUpdateCompletedEvent(eventId, updatedBrandDto);

        // When
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null;
        });

        // Then
        AtomicReference<ConsumerRecord<String, String>> recordRef = new AtomicReference<>();
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                return false;
            }
            recordRef.set(records.iterator().next());
            return true;
        });

        ConsumerRecord<String, String> record = recordRef.get();
        assertThat(record.topic()).isEqualTo(updatedTopic);

        Envelope<BrandUpdatedPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});
        BrandUpdatedPayload payload = envelope.payload();

        assertThat(payload.brand().brandId()).isEqualTo(1L);
        assertThat(payload.brand().name()).isEqualTo("Updated Kafka Brand");

        ProductOutboxEvent outboxEvent = outboxRepository.findByEventId(eventId).orElseThrow();
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SUCCEEDED);
    }

    @Test
    @DisplayName("브랜드 삭제 시 Kafka에 BrandDeleted 이벤트가 발행되어야 한다")
    void deleteBrand_shouldPublishDeleteEventToKafka() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        Long brandId = 1L;
        BrandDeletionCompletedEvent event = new BrandDeletionCompletedEvent(eventId, brandId);

        // When
        transactionTemplate.execute(status -> {
            applicationEventPublisher.publishEvent(event);
            return null;
        });

        // Then
        AtomicReference<ConsumerRecord<String, String>> recordRef = new AtomicReference<>();
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                return false;
            }
            recordRef.set(records.iterator().next());
            return true;
        });

        ConsumerRecord<String, String> record = recordRef.get();
        assertThat(record.topic()).isEqualTo(deletedTopic);

        Envelope<BrandDeletedPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});
        BrandDeletedPayload payload = envelope.payload();

        assertThat(payload.brandId()).isEqualTo(brandId);

        ProductOutboxEvent outboxEvent = outboxRepository.findByEventId(eventId).orElseThrow();
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SUCCEEDED);
    }
}