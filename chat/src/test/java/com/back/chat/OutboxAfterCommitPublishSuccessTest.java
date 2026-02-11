package com.back.chat;

import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxPollingProperties;
import com.back.chat.adapter.out.outbox.OutboxStatusUpdater;
import com.back.chat.event.ChatEventType;
import com.back.chat.event.ChatOutboxSavedEvent;
import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {OutboxPollingProperties.CHAT_BLIND_REQUESTED_TOPIC}
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class OutboxAfterCommitPublishSuccessTest {

    @Autowired ApplicationEventPublisher eventPublisher;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired ChatOutboxRepository outboxRepository;
    @Autowired EmbeddedKafkaBroker embeddedKafka;
    @Autowired JsonMapper jsonMapper;

    @MockitoBean OutboxStatusUpdater statusUpdater;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close();
    }

    @Test
    void AFTER_COMMIT_이후_비동기로_publish되고_성공이면_markSent_호출된다() throws Exception {
        // =========================
        // 1) consumer를 먼저 준비/구독 (가장 중요)
        // =========================
        Map<String, Object> props = consumerProps(embeddedKafka, "outbox-test-" + UUID.randomUUID());
        consumer = new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, OutboxPollingProperties.CHAT_BLIND_REQUESTED_TOPIC);

        // =========================
        // 2) given: outbox 저장 + AFTER_COMMIT 이벤트 발행 (트랜잭션 커밋 발생)
        // =========================
        UUID eventId = UUID.randomUUID();
        Long messageId = 1L;

        ChatMessageBlindedKafkaEvent payload = new ChatMessageBlindedKafkaEvent(
                eventId.toString(),
                123L,
                LocalDateTime.now()
        );
        String payloadJson = jsonMapper.writeValueAsString(payload);

        Long outboxId = transactionTemplate.execute(status -> {
            ChatOutbox saved = outboxRepository.save(
                    ChatOutbox.pending(
                            eventId,
                            "CHAT_MESSAGE",
                            messageId,
                            ChatEventType.MESSAGE_BLINDED,
                            payloadJson,
                            LocalDateTime.now()
                    )
            );

            eventPublisher.publishEvent(new ChatOutboxSavedEvent(saved.getId()));
            return saved.getId();
        });

        // =========================
        // 3) then: 토픽에 레코드가 들어왔는지 "안정적으로" 확인
        //    (getSingleRecord 대신 poll 루프)
        // =========================
        ConsumerRecord<String, String> record = pollForSingleRecord(
                consumer,
                Duration.ofSeconds(10)
        );

        assertThat(record).isNotNull();
        assertThat(record.topic()).isEqualTo(OutboxPollingProperties.CHAT_BLIND_REQUESTED_TOPIC);
        assertThat(record.value()).contains("MESSAGE_BLINDED");

        // =========================
        // 4) then: 성공 시 markSent 호출 (비동기 콜백 대기)
        // =========================
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(statusUpdater).markSent(eq(outboxId), any(LocalDateTime.class)));

        // (추가 안전장치) 실패로 타면 markFailed가 호출될 수도 있으니, 원하면 아래도 함께 두면 디버깅 쉬움
        // verify(statusUpdater, never()).markFailed(eq(outboxId), anyString(), any(LocalDateTime.class), anyInt());
    }

    /**
     * Spring Kafka Test 4.x: KafkaTestUtils.consumerProps(...) deprecated 회피
     */
    private static Map<String, Object> consumerProps(EmbeddedKafkaBroker broker, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    /**
     * getSingleRecord(...)가 flaky할 수 있어서 poll로 안정적으로 1건 확보
     */
    private static ConsumerRecord<String, String> pollForSingleRecord(
            Consumer<String, String> consumer,
            Duration maxWait
    ) {
        long deadline = System.currentTimeMillis() + maxWait.toMillis();
        ConsumerRecords<String, String> records;

        do {
            records = consumer.poll(Duration.ofMillis(300));
            if (!records.isEmpty()) {
                return records.iterator().next();
            }
        } while (System.currentTimeMillis() < deadline);

        return null;
    }
}


