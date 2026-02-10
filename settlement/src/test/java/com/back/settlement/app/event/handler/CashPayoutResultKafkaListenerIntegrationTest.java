package com.back.settlement.app.event.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.event.payload.PayoutResultPayload;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.fixture.SettlementFixture;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@Slf4j
@SpringJUnitConfig({KafkaTestConsumerConfig.class})
@EmbeddedKafka(
        partitions = 1,
        topics = CashPayoutResultKafkaListenerIntegrationTest.TOPIC
)
@DisplayName("캐시 지급 요청 결과를 받는 통합 테스트")
class CashPayoutResultKafkaListenerIntegrationTest {

    static final String TOPIC = "settlement-payout-result";

    private static final JsonMapper jsonMapper = new JsonMapper();

    @Autowired
    private KafkaTemplate<String, EventName> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private Settlement createSettlement(Long id, SettlementStatus status) {
        return SettlementFixture.createSettlement(id, 1L, "판매자", status);
    }

    private void publishToKafka(PayoutResultPayload payload) {
        log.info("Kafka 메시지 발행 시작");
        log.info("[발행 준비] PayoutResultPayload - settlementId={}, success={}, failReason={}", payload.settlementId(), payload.success(), payload.failReason());

        Envelope<PayoutResultPayload> event = Envelope.of("settlement.payout.result", payload);
        kafkaTemplate.send(TOPIC, event);
        kafkaTemplate.flush();
        log.info("[발행 완료] 토픽 '{}' 으로 메시지 전송됨", TOPIC);
    }

    private Envelope<PayoutResultPayload> consumeAndDeserialize() throws Exception {
        try (KafkaConsumer<String, String> consumer = createStringConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            log.info("[수신 대기] 토픽 '{}' 구독ing", TOPIC);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            log.info("[수신 완료] 수신된 메시지 개수: {}", records.count());

            assertThat(records.count()).isGreaterThanOrEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            log.info("[원본 JSON] {}", record.value());

            Envelope<PayoutResultPayload> envelope = jsonMapper.readValue(record.value(), new TypeReference<>() {});
            log.info("[역직렬화 완료] eventType={}, eventId={}", envelope.header().eventType(), envelope.header().eventId());
            log.info("[Payload] settlementId={}, success={}, failReason={}", envelope.payload().settlementId(), envelope.payload().success(), envelope.payload().failReason());

            return envelope;
        }
    }

    private String consumeRawMessage() {
        try (KafkaConsumer<String, String> consumer = createStringConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            log.info("[수신 대기] 토픽 '{}' 구독ing", TOPIC);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            log.info("[수신 완료] 수신된 메시지 개수: {}", records.count());

            assertThat(records.count()).isGreaterThanOrEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            log.info("[원본 JSON] {}", record.value());
            return record.value();
        }
    }

    private KafkaConsumer<String, String> createStringConsumer() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps("test-listener-group", "true", embeddedKafka));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    @Nested
    @DisplayName("Kafka 메시지 구조 검증")
    class MessageStructure {

        @Test
        @DisplayName("발행된 메시지의 header와 payload가 올바르게 직렬화/역직렬화된다")
        void messageIsSerializedAndDeserializedCorrectly() throws Exception {
            // given, when
            publishToKafka(new PayoutResultPayload(1L, true, null));
            Envelope<PayoutResultPayload> received = consumeAndDeserialize();

            // then
            assertThat(received.header().eventType()).isEqualTo("settlement.payout.result");
            assertThat(received.header().eventId()).isNotBlank();

            assertThat(received.payload().settlementId()).isEqualTo(1L);
            assertThat(received.payload().success()).isTrue();
            assertThat(received.payload().failReason()).isNull();
        }
    }

    @Nested
    @DisplayName("캐시 지급 성공 메시지 수신 시")
    class WhenSuccessMessageReceived {

        @Test
        @DisplayName("Kafka에서 수신한 성공 메시지를 리스너가 처리하면 정산이 COMPLETED된다")
        void completesSettlementViaKafka() throws Exception {
            // given
            Long settlementId = 10L;
            Settlement settlement = createSettlement(settlementId, SettlementStatus.IN_PROGRESS);

            SettlementRepository repository = mock(SettlementRepository.class);
            given(repository.findById(settlementId)).willReturn(Optional.of(settlement));
            given(repository.save(any(Settlement.class))).willAnswer(inv -> inv.getArgument(0));

            CashPayoutResultKafkaListener listener = new CashPayoutResultKafkaListener(repository, jsonMapper);
            log.info("[테스트 시작] 캐시 지급 성공 메시지 처리 테스트");

            // when
            publishToKafka(new PayoutResultPayload(settlementId, true, null));
            String received = consumeRawMessage();

            log.info("[리스너 호출] listener.listen() 실행");
            listener.listen(received);

            // then
            log.info("[처리 결과] 정산 상태: {} → {}", SettlementStatus.IN_PROGRESS, settlement.getStatus());
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);

            ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(settlementId);
            log.info("테스트 완료");
        }
    }

    @Nested
    @DisplayName("캐시 지급 실패 메시지 수신 시")
    class WhenFailureMessageReceived {

        @Test
        @DisplayName("Kafka에서 수신한 실패 메시지를 리스너가 처리하면 정산이 FAILED된다")
        void failsSettlementViaKafka() throws Exception {
            // given
            Long settlementId = 20L;
            String failReason = "잔액 부족";
            Settlement settlement = createSettlement(settlementId, SettlementStatus.IN_PROGRESS);

            SettlementRepository repository = mock(SettlementRepository.class);
            given(repository.findById(settlementId)).willReturn(Optional.of(settlement));
            given(repository.save(any(Settlement.class))).willAnswer(inv -> inv.getArgument(0));

            CashPayoutResultKafkaListener listener = new CashPayoutResultKafkaListener(repository, jsonMapper);
            log.info("[테스트 시작] 캐시 지급 실패 메시지 처리 테스트");

            // when
            publishToKafka(new PayoutResultPayload(settlementId, false, failReason));
            String received = consumeRawMessage();

            log.info("[리스너 호출] listener.listen()");
            listener.listen(received);

            // then
            log.info("[처리 결과] 정산 상태: {} → {}, 실패 사유: {}",
                    SettlementStatus.IN_PROGRESS, settlement.getStatus(), failReason);
            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);

            ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(settlementId);
            log.info("테스트 완료");
        }
    }

    @Nested
    @DisplayName("존재하지 않는 정산서 메시지 수신 시")
    class WhenSettlementNotFound {

        @Test
        @DisplayName("정산서가 없으면 도메인 이벤트를 발행하지 않는다")
        void doesNotPublishEventsWhenNotFound() throws Exception {
            // given
            Long settlementId = 999L;

            SettlementRepository repository = mock(SettlementRepository.class);
            given(repository.findById(settlementId)).willReturn(Optional.empty());

            CashPayoutResultKafkaListener listener = new CashPayoutResultKafkaListener(repository, jsonMapper);
            log.info("[테스트 시작] 정산서가 없는 경우 메시지 처리 테스트");

            // when
            publishToKafka(new PayoutResultPayload(settlementId, true, null));
            String received = consumeRawMessage();

            log.info("[리스너 호출] listener.listen() 실행...");
            listener.listen(received);

            // then
            log.info("[처리 결과] 정산서를 찾을 수 없음 (settlementId={}), save 호출 안 함", settlementId);
            then(repository).should(never()).save(any());
            log.info("테스트 완료");
        }
    }
}
