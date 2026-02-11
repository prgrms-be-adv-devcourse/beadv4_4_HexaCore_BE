package com.back.notification.adapter.in;

import com.back.common.settlement.event.SettlementCompletedEvent;
import com.back.common.market.event.BiddingFailedEvent;
import com.back.common.market.event.SellBiddingCreatedEvent;
import com.back.common.market.event.PurchaseCanceledEvent;
import com.back.common.market.event.BiddingCompletedEvent;
import com.back.common.product.event.InspectionCompletedEvent;
import com.back.common.user.event.FcmTokenChangedEvent;
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

    @KafkaListener(
            topics = "${custom.kafka.topic.market-order-completed}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(BiddingCompletedEvent event) {
        notificationFacade.notify(Type.BID_COMPLETED, event);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-order-deleted}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(PurchaseCanceledEvent event) {
        notificationFacade.notify(Type.PURCHASE_CANCELED, event);
    }

    // 30일 초과되어 입찰에 실패
    @KafkaListener(
            topics = "${custom.kafka.topic.market-bidding-deleted}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(BiddingFailedEvent event) {
        notificationFacade.notify(Type.BID_FAILED, event);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-inspect-completed}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(InspectionCompletedEvent event) {
        notificationFacade.notify(Type.INSPECTION_COMPLETED, event);
    }

    // 정산 완료
    @KafkaListener(
            topics = "${custom.kafka.topic.settlement-payout-completed}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(SettlementCompletedEvent event) {
        notificationFacade.notify(Type.SETTLEMENT_COMPLETED, event);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.market-bidding-sell-created}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(SellBiddingCreatedEvent event) {
        notificationFacade.notify(Type.PRICE_DROPPED, event);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-updated}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void handle(FcmTokenChangedEvent event) {
        notificationUserUsecase.updateFcmToken(event.userId(), event.fcmToken());
    }
}
