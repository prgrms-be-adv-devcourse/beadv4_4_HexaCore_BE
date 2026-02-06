package com.back.detector.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.detector.app.event.payload.HijackSuspectedPayload;
import com.back.detector.dto.HijackDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectorEventHandler {
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${custom.kafka.topic.detector-hijack-detected}")
    private String hijackDetectedTopic;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishHijackSuspected(HijackDetectedEvent event) {
        log.info("========== 핸들러 진입 확인 ==========");
        log.info("이벤트 수신: userId={}, email={}", event.userId(), event.email());
        Envelope<HijackSuspectedPayload> kafkaEvent = Envelope.of(
                "detector.hijack.detected",
                new HijackSuspectedPayload(
                        event.userId(),
                        event.email(),
                        event.existingIps(),
                        event.currentIp(),
                        event.transactionAmount(),
                        event.reason()
                )
        );
        try {
            log.info("""
                [계정 탈취 의심 이메일 발송]
                - 사용자ID: {}
                - 이메일: {}
                - 기존IP 목록: {}
                - 현재IP: {}
                - 거래금액: {}원
                - 알림사유: {}
                """,
                    event.userId(),
                    event.email(),
                    event.existingIps(),
                    event.currentIp(),
                    event.transactionAmount(),
                    event.reason()
            );

            // TODO: 실제 이메일 발송 로직 구현
            // emailService.sendHijackAlert(event);

            log.info("[계정 탈취 의심 이메일 발송 완료] userId: {}, email: {}",
                    event.userId(), event.email());
        } catch (Exception e) {
            log.error("[계정 탈취 의심 이메일 발송 실패] userId: {}, email: {}",
                    event.userId(), event.email(), e);
        }
        System.out.print("durl " + kafkaEvent);
        kafkaEventPublisher.publish(hijackDetectedTopic, kafkaEvent);
    }
}
