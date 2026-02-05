package com.back.notification.mapper;

import com.back.common.settlement.event.SettlementCompletedEvent;
import com.back.common.market.event.BiddingFailedEvent;
import com.back.common.market.event.SellBiddingCreatedEvent;
import com.back.common.market.event.PurchaseCanceledEvent;
import com.back.common.market.event.BiddingCompletedEvent;
import com.back.common.product.event.InspectionCompletedEvent;
import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.NotificationCreatedEvent;
import com.back.notification.dto.NotificationIdResponseDto;
import com.back.notification.dto.PushDispatchMessage;
import com.back.notification.dto.response.NotificationListResponseDto;
import com.back.notification.dto.response.NotificationResponseDto;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public Notification toSettlementCompletedNotification(Type type, SettlementCompletedEvent event, Long sellerId) {
        return Notification.builder()
                .userId(sellerId)
                .type(type)
                .content(Map.of(
                        "startAt", event.startAt(),
                        "endAt", event.endAt(),
                        "totalNetAmount", event.totalNetAmount()
                ))
                .deepLink("/settlements/" + event.sellerId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toPriceDroppedNotification(Type type, SellBiddingCreatedEvent event, Long userId) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "targetPrice", event.currentPrice(),
                        "productId", event.productId(),
                        "productName", event.productName(),
                        "productSize", event.productOption(),
                        "thumbnailImage", event.thumbnailImage(),
                        "brandName", event.brandName()
                ))
                .deepLink("/products/" + event.productId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public NotificationCreatedEvent toNotificationCreatedEvent(List<Notification> notifications) {
        return NotificationCreatedEvent.builder()
                .notificationIds(notifications
                        .stream()
                        .map(Notification::getId)
                        .toList()
                )
                .build();
    }

    public PushDispatchMessage toPushDispatchMessage(Notification notification, String fcmToken) {
        return PushDispatchMessage.builder()
                .title(notification.getTitle())
                .body(notification.getBody())
                .deepLink(notification.getDeepLink())
                .fcmToken(fcmToken)
                .build();
    }

    public NotificationIdResponseDto toNotificationIdResponseDto(Notification notification) {
        return NotificationIdResponseDto.builder()
                .id(notification.getId())
                .build();
    }

    public NotificationListResponseDto toNotificationListResponseDto(Slice<Notification> notifications) {
        return NotificationListResponseDto.builder()
                .notificationResponses(
                        notifications.getContent().stream()
                                .map(this::toNotificationResponseDto)
                                .toList()
                )
                .hasNext(notifications.hasNext())
                .build();
    }

    private NotificationResponseDto toNotificationResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .deepLink(notification.getDeepLink())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
