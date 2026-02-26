package com.back.notification.adapter.in;

import com.back.common.event.KafkaEventParser;
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
import com.back.notification.dto.payload.UserCreatedPayload;
import com.back.notification.dto.payload.UserSettingCreatedPayload;
import com.back.notification.dto.payload.UserSettingUpdatedPayload;
import com.back.notification.dto.payload.UserUpdatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationFacade notificationFacade;
    private final NotificationUserUsecase notificationUserUsecase;
    private final NotificationUserSettingUsecase notificationUserSettingUsecase;
    private final KafkaEventParser kafkaEventParser;

    @KafkaListener(
            topics = "${custom.kafka.topic.market-order-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBiddingCompleted(String message) {
        BiddingCompletedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationFacade.notify(Type.BID_COMPLETED, payload);
        log.info("[KafkaListenerSuccess] BiddingCompletedPayload 수신 : biddingId = {}", payload.biddingId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-order-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePurchaseCanceled(String message) {
        PurchaseCanceledPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationFacade.notify(Type.PURCHASE_CANCELED, payload);
        log.info("[KafkaListenerSuccess] PurchaseCanceledPayload 수신 : biddingId = {}", payload.biddingId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-bidding-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBiddingFailed(String message) {
        BiddingFailedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationFacade.notify(Type.BID_FAILED, payload);
        log.info("[KafkaListenerSuccess] BiddingFailedPayload 수신 : productId = {}", payload.productId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-inspect-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleInspectionCompleted(String message) {
        InspectionCompletedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationFacade.notify(Type.INSPECTION_COMPLETED, payload);
        log.info("[KafkaListenerSuccess] InspectionCompletedPayload 수신 : productId = {}", payload.productId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.settlement-payout-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleSettlementCompleted(String message) {
        SettlementCompletedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationFacade.notify(Type.SETTLEMENT_COMPLETED, payload);
        log.info("[KafkaListenerSuccess] SettlementCompletedPayload 수신 : sellerId = {}", payload.sellerId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-bidding-sell-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleSellBiddingCreated(String message) {
        SellBiddingCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationFacade.notify(Type.PRICE_DROPPED, payload);
        log.info("[KafkaListenerSuccess] SellBiddingCreatedPayload 수신 : productId = {}", payload.productId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-fcm-token-changed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleFcmTokenChanged(String message) {
        FcmTokenChangedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationUserUsecase.updateFcmToken(payload.userId(), payload.fcmToken());
        log.info("[KafkaListenerSuccess] FcmTokenChangedPayload 수신 : userId = {}", payload.userId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-setting-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserSettingCreated(String message) {
        UserSettingCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationUserSettingUsecase.createUserSetting(
                payload.userId(),
                payload.bidStatusEnabled(),
                payload.productStatusEnabled(),
                payload.priceEnabled(),
                payload.settlementEnabled()
        );
        log.info("[KafkaListenerSuccess] UserSettingCreatedPayload 수신 : userId = {}", payload.userId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-setting-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserSettingUpdated(String message) {
        UserSettingUpdatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationUserSettingUsecase.updateUserSetting(
                payload.userId(),
                payload.bidStatusEnabled(),
                payload.productStatusEnabled(),
                payload.priceEnabled(),
                payload.settlementEnabled()
        );
        log.info("[KafkaListenerSuccess] UserSettingUpdatedPayload 수신 : userId = {}", payload.userId());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserCreated(String message) {
        UserCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationUserUsecase.createUser(payload.id(), payload.nickname(), payload.email());
        log.info("[KafkaListenerSuccess] UserCreatedPayload 수신 : userId = {}", payload.id());
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserUpdated(String message) {
        UserUpdatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        notificationUserUsecase.updateUser(payload.id(), payload.nickname());
        log.info("[KafkaListenerSuccess] UserUpdatedPayload 수신 : userId = {}", payload.id());
    }
}
