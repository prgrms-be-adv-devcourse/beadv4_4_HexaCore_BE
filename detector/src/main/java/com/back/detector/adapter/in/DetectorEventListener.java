package com.back.detector.adapter.in;

import com.back.common.user.event.UserCreatedEvent;
import com.back.detector.app.HijackDetector;
import com.back.detector.event.HijackSuspectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetectorEventListener {

    private final HijackDetector hijackDetector;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-created}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            if (event.ipAddress() == null || event.ipAddress().isEmpty()) {
                log.warn("[회원가입 IP 이벤트] IP 정보 없음 - 스킵, userId: {}", event.id());
                return;
            }

            // 회원가입 IP를 안전한 IP로 등록 (쿨다운 없음)
            hijackDetector.registerInitialIp(event.id(), event.ipAddress());
            
            log.info("[회원가입 초기 IP 등록 완료] userId: {}, IP: {} (쿨다운 없음)",
                    event.id(), event.ipAddress());
        } catch (Exception e) {
            log.error("[회원가입 초기 IP 등록 실패] userId: {}, IP: {}",
                    event.id(), event.ipAddress(), e);
        }
    }

    /**
     * 계정 탈취 의심 이벤트 처리
     * 새 IP에서 20만원 초과 거래 시 사용자에게 이메일 발송
     */
    @EventListener
    public void handleHijackSuspected(HijackSuspectedEvent event) {
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
    }
}
