package com.back.cash;

import com.back.cash.app.usecase.CreateWalletUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {"${custom.kafka.topic.user-wallet-created}",
                "${custom.kafka.topic.user-wallet-created-dlt}"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@Slf4j
class WalletUserCreatedListenerRetryTest {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    CreateWalletUseCase createWalletUseCase;

    @Value("${custom.kafka.topic.user-wallet-created}")
    String topic;

    @Value("${custom.kafka.topic.user-wallet-created-dlt}")
    String dltTopic;

    static final String MESSAGE = """
            {
              "header": {
                "eventId": "test-wallet-event-001",
                "eventType": "user.wallet.created",
                "occurrenceAt": "2024-01-01T00:00:00"
              },
              "payload": {
                "userId": 42
              }
            }
            """;

    static final String MALFORMED_MESSAGE = """
            {
              "header": {
                "eventId": "wallet-parse-failed-event",
                "eventType": "user.wallet.created",
                "occurrenceAt": "2024-01-01T00:00:00"
              },
              "payload": "this-is-not-object"
            }
            """;

    @Test
    @DisplayName("역직렬화 실패 메시지는 재시도 없이 DLT로 전송된다")
    void parseFailure_sendsToDltWithoutRetry(
            @Autowired EmbeddedKafkaBroker broker
    ) throws Exception {
        BlockingQueue<ConsumerRecord<String, String>> dltRecords = new LinkedBlockingQueue<>();
        KafkaMessageListenerContainer<String, String> dltContainer =
                createDltConsumer(broker, dltTopic, dltRecords);
        dltContainer.start();
        ContainerTestUtils.waitForAssignment(dltContainer, 1);

        kafkaTemplate.send(topic, MALFORMED_MESSAGE);

        ConsumerRecord<String, String> dltRecord = dltRecords.poll(10, TimeUnit.SECONDS);
        assertThat(dltRecord).isNotNull();
        assertThat(dltRecord.value()).contains("wallet-parse-failed-event");
        verify(createWalletUseCase, times(0)).createWallet(anyLong());

        dltContainer.stop();
    }

    @Test
    @DisplayName("재시도 가능한 예외 발생 시 3번 재시도 후 DLT로 전송된다")
    void retryableException_retriesThenSendsToDlt(
            @Autowired EmbeddedKafkaBroker broker
    ) throws Exception {
        // given
        doThrow(new RuntimeException("일시적 DB 장애"))
                .when(createWalletUseCase).createWallet(anyLong());

        BlockingQueue<ConsumerRecord<String, String>> dltRecords = new LinkedBlockingQueue<>();
        KafkaMessageListenerContainer<String, String> dltContainer =
                createDltConsumer(broker, dltTopic, dltRecords);
        dltContainer.start();
        ContainerTestUtils.waitForAssignment(dltContainer, 1);

        // when
        kafkaTemplate.send(topic, MESSAGE);

        // then
        // 초기 1번 + 재시도 3번 = 총 4번 호출
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        verify(createWalletUseCase, times(4)).createWallet(anyLong())
                );

        ConsumerRecord<String, String> dltRecord = dltRecords.poll(10, TimeUnit.SECONDS);
        assertThat(dltRecord).isNotNull();
        assertThat(dltRecord.value()).contains("42");

        int totalCalls = mockingDetails(createWalletUseCase).getInvocations().size();
        log.info(">>> [TEST] createWallet() 호출 횟수: {}", totalCalls);
        log.info(">>> [TEST] DLT 메시지 수신 확인 완료");

        dltContainer.stop();
    }

    private KafkaMessageListenerContainer<String, String> createDltConsumer(
            EmbeddedKafkaBroker broker,
            String dltTopic,
            BlockingQueue<ConsumerRecord<String, String>> records
    ) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-group-" + System.nanoTime(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"
        );

        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(props);
        ContainerProperties containerProps = new ContainerProperties(dltTopic);
        containerProps.setMessageListener((MessageListener<String, String>) records::add);

        return new KafkaMessageListenerContainer<>(cf, containerProps);
    }
}
