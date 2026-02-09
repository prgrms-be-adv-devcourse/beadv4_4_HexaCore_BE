package com.back.settlement.adapter.in.stub;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.settlement.app.event.payload.PayoutRequestPayload;
import com.back.settlement.app.event.payload.PayoutResultPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class CashPayoutStubListener {
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper;
    private final String resultTopic;

    public CashPayoutStubListener(KafkaEventPublisher kafkaEventPublisher, ObjectMapper objectMapper, @Value("${kafka.topic.cash-payout-completed}") String resultTopic) {
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.objectMapper = objectMapper;
        this.resultTopic = resultTopic;
    }

    @KafkaListener(
            topics = "${kafka.topic.settlement-payout-request}",
            groupId = "settlement-stub-group"
    )
    public void handlePayoutRequest(String message) {
        Envelope<PayoutRequestPayload> event;
        try {
            event = objectMapper.readValue(message, new TypeReference<Envelope<PayoutRequestPayload>>(){});
        } catch (Exception e) {
            log.error("[LOCAL STUB] 캐시 지급 요청 메시지 역직렬화 실패. message={}", message, e);
            return;
        }

        PayoutRequestPayload request = event.payload();
        log.info("[LOCAL STUB] 캐시 지급 요청 수신. settlementId={}, payeeId={}, netAmount={}",
                request.settlementId(), request.payeeId(), request.totalNetAmount());

        Envelope<PayoutResultPayload> result = Envelope.of(
                "cash.payout.completed",
                new PayoutResultPayload(request.settlementId(), true, null)
        );

        kafkaEventPublisher.publish(resultTopic, result);
        log.info("[LOCAL STUB] 캐시 지급 성공 응답 발행. settlementId={}", request.settlementId());
    }
}
