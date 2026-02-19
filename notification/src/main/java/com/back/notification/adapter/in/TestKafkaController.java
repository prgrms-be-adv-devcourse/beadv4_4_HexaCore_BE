package com.back.notification.adapter.in;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.notification.dto.payload.BiddingCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/test/kafka")
@RequiredArgsConstructor
public class TestKafkaController {
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${custom.kafka.topic.market-order-completed}")
    private String marketOrderCompletedTopic;

    @Value("${custom.kafka.topic.user-account-updated}")
    private String userAccountUpdatedTopic;

    @PostMapping("/bidding-completed")
    public String sendBiddingCompletedEvent() {
        BiddingCompletedPayload payload = BiddingCompletedPayload.builder()
                .biddingId(1L)
                .buyerUserId(100L)
                .sellerUserId(200L)
                .productId(1000L)
                .productName("나이키 에어포스 1")
                .productSize("270")
                .thumbnailImage("https://example.com/image.jpg")
                .brandName("Nike")
                .price(new BigDecimal("150000"))
                .biddingPosition("구매입찰")
                .build();

        Envelope<BiddingCompletedPayload> event = Envelope.of(marketOrderCompletedTopic, payload);
        kafkaEventPublisher.publish(marketOrderCompletedTopic, event);

        log.info("[TestKafkaController] Sent BiddingCompletedPayload for biddingId: {}", payload.biddingId());
        return "BiddingCompletedPayload 발행 완료: biddingId=" + payload.biddingId();
    }

    @PostMapping("/user-account-updated")
    public String sendUserAccountUpdatedEvent() {
        // FcmTokenChangedPayload와 다른 값을 가진 테스트용 Payload
        TestUserAccountUpdatedPayload payload = new TestUserAccountUpdatedPayload(
                1L,  // 다른 userId
                "different-fcm-token-xyz789",  // 다른 fcmToken
                "extra-field-for-testing"  // 추가 필드
        );

        Envelope<TestUserAccountUpdatedPayload> event = Envelope.of(userAccountUpdatedTopic, payload);
        kafkaEventPublisher.publish(userAccountUpdatedTopic, event);

        log.info("[TestKafkaController] Sent TestUserAccountUpdatedPayload for userId: {}", payload.userId());
        return "TestUserAccountUpdatedPayload 발행 완료: userId=" + payload.userId();
    }

    // 컨트롤러 내부에 정의한 테스트용 Payload
    private record TestUserAccountUpdatedPayload(
            Long userId,
            String fcmToken,
            String extraField  // FcmTokenChangedPayload에 없는 추가 필드
    ) implements com.back.common.event.KafkaPayload {
    }
}