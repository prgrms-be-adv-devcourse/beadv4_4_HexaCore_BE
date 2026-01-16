package com.back.notification.mapper;

import com.back.common.market.event.BiddingFailedEvent;
import com.back.common.market.event.PurchaseCanceledEvent;
import com.back.common.market.event.BiddingCompletedEvent;
import com.back.common.product.event.InspectionCompletedEvent;
import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationMapper {

    public Notification toBidCompletedNotification(Type type, BiddingCompletedEvent event,
                                                   Long userId, NotificationTargetRole role) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "biddingId", event.biddingId(),
                        "productId", event.productId(),
                        "productName", event.productName(),
                        "productSize", event.productSize(),
                        "brandName", event.brandName(),
                        "thumbnailImage", event.thumbnailImage(),
                        "price", event.price(),
                        "biddingPosition", event.biddingPosition(),
                        "role", role
                ))
                .deepLink("/biddings/" + event.biddingId())   // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toPurchaseCanceledNotification(Type type, PurchaseCanceledEvent event,
                                                       Long userId, NotificationTargetRole role) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "biddingId", event.biddingId(),
                        "productId", event.productId(),
                        "productName", event.productName(),
                        "productSize", event.productSize(),
                        "price", event.price(),
                        "biddingPosition", event.biddingPosition(),
                        "role", role
                ))
                .deepLink("/biddings/" + event.biddingId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toBidFailedNotification(Type type, BiddingFailedEvent event,
                                                Long userId, NotificationTargetRole role) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "productId", event.productId(),
                        "productName", event.productName(),
                        "productSize", event.productSize(),
                        "price", event.price(),
                        "biddingPosition", event.biddingPosition(),
                        "role", role
                ))
                .deepLink("/products/" + event.productId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toInspectionCompletedNotification(Type type, InspectionCompletedEvent event, Long sellerId) {
        return Notification.builder()
                .userId(sellerId)
                .type(type)
                .content(Map.of(
                        "requestedAt", event.requestedAt(),
                        "productId", event.productId(),
                        "productName", event.productName(),
                        "productSize", event.productSize(),
                        "price", event.price(),
                        "productNumber", event.productNumber()
                ))
                .deepLink("/biddings/" + event.productId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }
}
