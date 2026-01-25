package com.back.notification.adapter.in;

import com.back.common.Settlement.event.SettlementCompletedEvent;
import com.back.common.market.event.BiddingFailedEvent;
import com.back.common.market.event.SellBiddingCreatedEvent;
import com.back.common.market.event.PurchaseCanceledEvent;
import com.back.common.market.event.BiddingCompletedEvent;
import com.back.common.product.event.InspectionCompletedEvent;
import com.back.common.user.event.fcmTokenChangedEvent;
import com.back.notification.app.NotificationFacade;
import com.back.notification.app.NotificationUserUsecase;
import com.back.notification.domain.enums.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationFacade notificationFacade;
    private final NotificationUserUsecase notificationUserUsecase;

    @KafkaListener(topics = "BiddingCompletedEvent", groupId = "PostEventListener__handle__1")
    public void handle(BiddingCompletedEvent event) {
        notificationFacade.notify(Type.BID_COMPLETED, event);
    }

    @KafkaListener(topics = "PurchaseCanceledEvent", groupId = "PostEventListener__handle__2")
    public void handle(PurchaseCanceledEvent event) {
        notificationFacade.notify(Type.PURCHASE_CANCELED, event);
    }

    // 30일 초과되어 입찰에 실패
    @KafkaListener(topics = "BiddingFailedEvent", groupId = "PostEventListener__handle__3")
    public void handle(BiddingFailedEvent event) {
        notificationFacade.notify(Type.BID_FAILED, event);
    }

    @KafkaListener(topics = "InspectionCompletedEvent", groupId = "PostEventListener__handle__1")
    public void handle(InspectionCompletedEvent event) {
        notificationFacade.notify(Type.INSPECTION_COMPLETED, event);
    }

    // 정산 완료
    @KafkaListener(topics = "SettlementCompletedEvent", groupId = "PostEventListener__handle__4")
    public void handle(SettlementCompletedEvent event) {
        notificationFacade.notify(Type.SETTLEMENT_COMPLETED, event);
    }

    @KafkaListener(topics = "SellBiddingCreatedEvent", groupId = "PostEventListener__handle__5")
    public void handle(SellBiddingCreatedEvent event) {
        notificationFacade.notify(Type.PRICE_DROPPED, event);
    }

    @KafkaListener(topics = "fcmTokenChangedEvent", groupId = "PostEventListener__handle__6")
    public void handle(fcmTokenChangedEvent event) {
        notificationUserUsecase.updateFcmToken(event.userId(), event.fcmToken());
    }
}
