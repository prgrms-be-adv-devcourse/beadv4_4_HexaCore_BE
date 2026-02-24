package com.back.notification.adapter.in;

import com.back.common.event.Envelope;
import com.back.notification.app.NotificationFacade;
import com.back.notification.app.NotificationUserSettingUsecase;
import com.back.notification.app.NotificationUserUsecase;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.BiddingCompletedPayload;
import com.back.notification.dto.payload.BiddingFailedPayload;
import com.back.notification.dto.payload.FcmTokenChangedPayload;
import com.back.notification.dto.payload.InspectionCompletedPayload;
import com.back.notification.dto.payload.PurchaseCanceledPayload;
import com.back.notification.dto.payload.SellBiddingCreatedPayload;
import com.back.notification.dto.payload.SettlementCompletedPayload;
import com.back.notification.dto.payload.UserSettingCreatedPayload;
import com.back.notification.dto.payload.UserSettingUpdatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationFacade notificationFacade;
    private final NotificationUserUsecase notificationUserUsecase;
    private final NotificationUserSettingUsecase notificationUserSettingUsecase;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.market-order-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBiddingCompleted(String message) {
        try {
            Envelope<BiddingCompletedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            BiddingCompletedPayload payload = envelope.payload();
            
            notificationFacade.notify(Type.BID_COMPLETED, payload);
            log.info("[KafkaListenerSuccess] BiddingCompletedPayload 수신 : biddingId = {}", payload.biddingId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] BiddingCompletedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
        // TODO: DLQ 등 추가
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-order-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePurchaseCanceled(String message) {
        try {
            Envelope<PurchaseCanceledPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            PurchaseCanceledPayload payload = envelope.payload();
            
            notificationFacade.notify(Type.PURCHASE_CANCELED, payload);
            log.info("[KafkaListenerSuccess] PurchaseCanceledPayload 수신 : biddingId = {}", payload.biddingId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] PurchaseCanceledPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    // 30일 초과되어 입찰에 실패
    @KafkaListener(
            topics = "${custom.kafka.topic.market-bidding-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBiddingFailed(String message) {
        try {
            Envelope<BiddingFailedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            BiddingFailedPayload payload = envelope.payload();
            
            notificationFacade.notify(Type.BID_FAILED, payload);
            log.info("[KafkaListenerSuccess] BiddingFailedPayload 수신 : productId = {}", payload.productId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] BiddingFailedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-inspect-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleInspectionCompleted(String message) {
        try {
            Envelope<InspectionCompletedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            InspectionCompletedPayload payload = envelope.payload();
            
            notificationFacade.notify(Type.INSPECTION_COMPLETED, payload);
            log.info("[KafkaListenerSuccess] InspectionCompletedPayload 수신 : productId = {}", payload.productId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] InspectionCompletedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.settlement-payout-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleSettlementCompleted(String message) {
        try {
            Envelope<SettlementCompletedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            SettlementCompletedPayload payload = envelope.payload();
            
            notificationFacade.notify(Type.SETTLEMENT_COMPLETED, payload);
            log.info("[KafkaListenerSuccess] SettlementCompletedPayload 수신 : sellerId = {}", payload.sellerId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] SettlementCompletedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-bidding-sell-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleSellBiddingCreated(String message) {
        try {
            Envelope<SellBiddingCreatedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            SellBiddingCreatedPayload payload = envelope.payload();
            
            notificationFacade.notify(Type.PRICE_DROPPED, payload);
            log.info("[KafkaListenerSuccess] SellBiddingCreatedPayload 수신 : productId = {}", payload.productId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] SellBiddingCreatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-fcm-token-changed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleFcmTokenChanged(String message) {
        try {
            Envelope<FcmTokenChangedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            FcmTokenChangedPayload payload = envelope.payload();

            notificationUserUsecase.updateFcmToken(payload.userId(), payload.fcmToken());
            log.info("[KafkaListenerSuccess] FcmTokenChangedPayload 수신 : userId = {}", payload.userId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] FcmTokenChangedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-setting-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserSettingCreated(String message) {
        try {
            Envelope<UserSettingCreatedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            UserSettingCreatedPayload payload = envelope.payload();

            notificationUserSettingUsecase.createUserSetting(
                    payload.userId(),
                    payload.bidStatusEnabled(),
                    payload.productStatusEnabled(),
                    payload.priceEnabled(),
                    payload.settlementEnabled()
            );
            log.info("[KafkaListenerSuccess] UserSettingCreatedPayload 수신 : userId = {}", payload.userId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] UserSettingCreatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-setting-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserSettingUpdated(String message) {
        try {
            Envelope<UserSettingUpdatedPayload> envelope = jsonMapper.readValue(message, new TypeReference<>() {});
            UserSettingUpdatedPayload payload = envelope.payload();

            notificationUserSettingUsecase.updateUserSetting(
                    payload.userId(),
                    payload.bidStatusEnabled(),
                    payload.productStatusEnabled(),
                    payload.priceEnabled(),
                    payload.settlementEnabled()
            );
            log.info("[KafkaListenerSuccess] UserSettingUpdatedPayload 수신 : userId = {}", payload.userId());
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] UserSettingUpdatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}
