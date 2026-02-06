package com.back.settlement.app.event.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.back.common.event.Envelope;
import com.back.settlement.app.event.payload.PayoutRequestPayload;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.domain.event.SettlementStartedEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@Slf4j
@SpringJUnitConfig({KafkaTestProducerConfig.class, KafkaTestConsumerConfig.class})
@EmbeddedKafka(
        partitions = 1,
        topics = "settlement-payout-request"
)
@DisplayName("캐시 지급 요청 통합 테스트")
class SettlementCashPayoutEventHandlerIntegrationTest {

    private static final String TOPIC = "settlement-payout-request";

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private SettlementCashPayoutEventHandler handler;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    @DisplayName("이벤트 발행 시 Kafka 토픽에 메시지가 실제로 도착한다")
    void messageArrivesOnTopic() throws Exception {
        // given
        SettlementStartedEvent event = new SettlementStartedEvent(
                1L, SettlementStatus.PENDING, 42L,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(90000),
                BigDecimal.valueOf(10000)
        );

        log.info("캐시 지급 요청 이벤트 발행 시작 ");
        log.info("[발행] SettlementStartedEvent - settlementId={}, sellerId={}, totalGrossAmount={}", event.settlementId(), event.sellerId(), event.totalGrossAmount());

        // when
        handler.settlementStartedEvent(event);
        log.info("[발행 완료] Kafka 토픽 '{}' 으로 메시지 전송됨", TOPIC);

        // then
        try (KafkaConsumer<String, String> consumer = createConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            log.info("[수신 대기] 토픽 '{}' 구독ing", TOPIC);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            log.info("[수신 완료] 수신된 메시지 개수: {}", records.count());

            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            log.info("[원본 JSON] {}", record.value());

            Envelope<PayoutRequestPayload> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});

            log.info("[역직렬화 완료] eventType={}, eventId={}", envelope.header().eventType(), envelope.header().eventId());

            PayoutRequestPayload payload = envelope.payload();
            log.info("[Payload] settlementId={}, payeeId={}, totalGrossAmount={}, totalNetAmount={}, totalFeeAmount={}", payload.settlementId(), payload.payeeId(), payload.totalGrossAmount(), payload.totalNetAmount(), payload.totalFeeAmount());

            // header 검증
            assertThat(envelope.header().eventType()).isEqualTo("settlement.payout.requested");
            assertThat(envelope.header().eventId()).isNotBlank();

            // payload 검증
            assertThat(payload.settlementId()).isEqualTo(1L);
            assertThat(payload.payeeId()).isEqualTo(42L);
            log.info("테스트 완료");
        }
    }

    private KafkaConsumer<String, String> createConsumer() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(props);
    }
}
