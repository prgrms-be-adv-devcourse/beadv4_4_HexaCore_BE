package com.back.product.app;

import com.back.common.event.Envelope;
import com.back.product.adapter.out.document.ProductDocumentRepository;
import com.back.product.adapter.out.event.BrandSpringEventPublisher;
import com.back.product.dto.model.BrandDto;
import com.back.product.event.kafka.BrandCreatedPayload;
import com.back.product.event.kafka.BrandDeletedPayload;
import com.back.product.event.kafka.BrandUpdatedPayload;
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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
class BrandEventIntegrationTest {

    @MockitoBean
    private ProductDocumentRepository productDocumentRepository;

    @Autowired
    private BrandSpringEventPublisher brandSpringEventPublisher;

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
    }

    @Test
    @DisplayName("브랜드 생성 시 Kafka에 BrandCreated 이벤트가 발행되어야 한다")
    void createBrand_shouldPublishCreateEventToKafka() throws Exception {
        // Given
        // ProductFacade를 호출하지 않으므로, 더미 BrandDto를 직접 생성하여 이벤트에 전달
        List<BrandDto> brandDtos = List.of(new BrandDto(1L, "New Kafka Brand", "https://thumbNail.png"));
        Long createdBrandId = brandDtos.get(0).brandId(); // assertion을 위한 ID

        // When
        // TransactionTemplate을 사용하여 트랜잭션 내에서 Spring 이벤트를 발행
        transactionTemplate.execute(status -> {
            brandSpringEventPublisher.sendCreatedEvent(brandDtos);
            return null;
        });

        // Then
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records.count()).isEqualTo(1);

        ConsumerRecord<String, String> record = records.iterator().next();
        assertThat(record.topic()).isEqualTo(createdTopic);

        Envelope<BrandCreatedPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});
        BrandCreatedPayload payload = envelope.payload();

        assertThat(payload.brands()).hasSize(1);
        assertThat(payload.brands().get(0).brandId()).isEqualTo(createdBrandId);
        assertThat(payload.brands().get(0).name()).isEqualTo("New Kafka Brand");
    }

    @Test
    @DisplayName("브랜드 수정 시 Kafka에 BrandUpdated 이벤트가 발행되어야 한다")
    void modifyBrand_shouldPublishUpdateEventToKafka() throws Exception {
        // Given
        // ProductFacade를 호출하지 않으므로, 더미 BrandDto를 직접 생성
        Long brandId = 1L; // 더미 ID
        BrandDto updatedBrandDto = new BrandDto(brandId, "Updated Kafka Brand", "http://image2.png");

        // When
        // TransactionTemplate을 사용하여 트랜잭션 내에서 Spring 이벤트를 발행
        transactionTemplate.execute(status -> {
            brandSpringEventPublisher.sendUpdatedEvent(updatedBrandDto);
            return null;
        });

        // Then
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records.count()).isEqualTo(1);

        ConsumerRecord<String, String> record = records.iterator().next();
        assertThat(record.topic()).isEqualTo(updatedTopic);

        Envelope<BrandUpdatedPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});
        BrandUpdatedPayload payload = envelope.payload();

        assertThat(payload.brand().brandId()).isEqualTo(brandId);
        assertThat(payload.brand().name()).isEqualTo("Updated Kafka Brand");
    }

    @Test
    @DisplayName("브랜드 삭제 시 Kafka에 BrandDeleted 이벤트가 발행되어야 한다")
    void deleteBrand_shouldPublishDeleteEventToKafka() throws Exception {
        // Given
        // ProductFacade를 호출하지 않으므로, 더미 brandId를 직접 사용
        Long brandId = 1L; // 더미 ID

        // When
        // TransactionTemplate을 사용하여 트랜잭션 내에서 Spring 이벤트를 발행
        transactionTemplate.execute(status -> {
            brandSpringEventPublisher.sendDeletedEvent(brandId);
            return null;
        });

        // Then
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records.count()).isEqualTo(1);

        ConsumerRecord<String, String> record = records.iterator().next();
        assertThat(record.topic()).isEqualTo(deletedTopic);

        Envelope<BrandDeletedPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});
        BrandDeletedPayload payload = envelope.payload();

        assertThat(payload.brandId()).isEqualTo(brandId);
    }
}
