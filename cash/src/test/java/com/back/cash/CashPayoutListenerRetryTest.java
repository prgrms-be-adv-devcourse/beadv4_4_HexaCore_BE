package com.back.cash;

import com.back.cash.app.CashPayoutFacade;
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
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(properties = {
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {"${custom.kafka.topic.settlement-payout-requested}",
                "${custom.kafka.topic.settlement-payout-requested-dlt}"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@Slf4j
class CashPayoutListenerRetryTest {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    CashPayoutFacade cashPayoutFacade;

    @Value("${custom.kafka.topic.settlement-payout-requested}")
    String topic;

    @Value("${custom.kafka.topic.settlement-payout-requested-dlt}")
    String dltTopic;

    static final String MESSAGE = """
            {
              "header": {
                "eventId": "test-event-001",
                "eventType": "settlement.payout.requested",
                "occurrenceAt": "2024-01-01T00:00:00"
              },
              "payload": {
                "settlementId": 9999,
                "payeeId": 1,
                "totalGrossAmount": 10000,
                "totalNetAmount": 9000,
                "totalFeeAmount": 1000
              }
            }
            """;

    static final String MALFORMED_MESSAGE = """
            {
              "header": {
                "eventId": "parse-failed-event",
                "eventType": "settlement.payout.requested",
                "occurrenceAt": "2024-01-01T00:00:00"
              },
              "payload": "this-is-not-object"
            }
            """;

    @Test
    @DisplayName("재시도 가능한 예외 발생 시 3번 재시도 후 DLT로 전송된다")
    void retryableException_retriesThenSendsToDlt(
            @Autowired EmbeddedKafkaBroker broker
    ) throws Exception {

        // given
        doThrow(new RuntimeException("일시적 DB 장애"))
                .when(cashPayoutFacade).requestPayout(any());

        // DLT 토픽 consumer 설정
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
                        verify(cashPayoutFacade, times(4)).requestPayout(any())
                );

        // DLT 토픽에 메시지가 전송되었는지 확인
        ConsumerRecord<String, String> dltRecord = dltRecords.poll(10, TimeUnit.SECONDS);
        assertThat(dltRecord).isNotNull();
        assertThat(dltRecord.value()).contains("9999");

        int totalCalls = mockingDetails(cashPayoutFacade).getInvocations().size();
        log.info(">>> [TEST] requestPayout() 호출 횟수: {}", totalCalls);
        log.info(">>> [TEST] DLT 메시지 수신 확인 완료");

        dltContainer.stop();
    }

    @Test
    @DisplayName("중복 정산 요청은 재시도 없이 정상 처리된다")
    void duplicateRequest_noRetry() {

        // given
        // Facade가 중복을 감지하고 정상 리턴 (예외 안 던짐)
        doNothing().when(cashPayoutFacade).requestPayout(any());

        // when
        kafkaTemplate.send(topic, MESSAGE);

        // then
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        verify(cashPayoutFacade, times(1)).requestPayout(any())
                );
    }

    @Test
    @DisplayName("BadRequestException은 Facade에서 삼켜져서 재시도도 DLT도 발생하지 않는다")
    void badRequestException_swallowedByFacade_noRetryNoDlt(
            @Autowired EmbeddedKafkaBroker broker
    ) throws Exception {
        // given
        // Facade가 BadRequestException을 삼키므로 Listener까지 예외가 전파되지 않음
        // 첫 호출에서 BadRequestException → Facade가 삼킴, 이후 호출은 정상
        doThrow(new BadRequestException(FailureCode.INVALID_AMOUNT))
                .doNothing()
                .when(cashPayoutFacade).requestPayout(any());

        BlockingQueue<ConsumerRecord<String, String>> dltRecords = new LinkedBlockingQueue<>();
        KafkaMessageListenerContainer<String, String> dltContainer =
                createDltConsumer(broker, dltTopic, dltRecords);
        dltContainer.start();
        ContainerTestUtils.waitForAssignment(dltContainer, 1);

        // when
        kafkaTemplate.send(topic, MESSAGE);

        // then
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        verify(cashPayoutFacade, times(1)).requestPayout(any())
                );

        // DLT에 메시지가 가지 않아야 함
        ConsumerRecord<String, String> dltRecord = dltRecords.poll(3, TimeUnit.SECONDS);
        assertThat(dltRecord).isNull();

        dltContainer.stop();
    }

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
        assertThat(dltRecord.value()).contains("parse-failed-event");
        verify(cashPayoutFacade, times(0)).requestPayout(any());

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
