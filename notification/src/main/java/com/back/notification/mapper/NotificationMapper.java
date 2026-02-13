package com.back.notification.mapper;

import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.NotificationCreatedEvent;
import com.back.notification.dto.NotificationIdResponseDto;
import com.back.notification.dto.PushDispatchMessage;
import com.back.notification.dto.payload.*;
import com.back.notification.dto.response.NotificationListResponseDto;
import com.back.notification.dto.response.NotificationResponseDto;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotificationMapper {

    public Notification toBidCompletedNotification(Type type, BiddingCompletedPayload payload,
                                                   Long userId, NotificationTargetRole role) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "biddingId", payload.biddingId(),
                        "productId", payload.productId(),
                        "productName", payload.productName(),
                        "productSize", payload.productSize(),
                        "brandName", payload.brandName(),
                        "thumbnailImage", payload.thumbnailImage(),
                        "price", payload.price(),
                        "biddingPosition", payload.biddingPosition(),
                        "role", role
                ))
                .deepLink("/biddings/" + payload.biddingId())   // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toPurchaseCanceledNotification(Type type, PurchaseCanceledPayload payload,
                                                       Long userId, NotificationTargetRole role) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "biddingId", payload.biddingId(),
                        "productId", payload.productId(),
                        "productName", payload.productName(),
                        "productSize", payload.productSize(),
                        "price", payload.price(),
                        "biddingPosition", payload.biddingPosition(),
                        "role", role
                ))
                .deepLink("/biddings/" + payload.biddingId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toBidFailedNotification(Type type, BiddingFailedPayload payload,
                                                Long userId, NotificationTargetRole role) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "productId", payload.productId(),
                        "productName", payload.productName(),
                        "productSize", payload.productSize(),
                        "price", payload.price(),
                        "biddingPosition", payload.biddingPosition(),
                        "role", role
                ))
                .deepLink("/products/" + payload.productId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toInspectionCompletedNotification(Type type, InspectionCompletedPayload payload, Long sellerId) {
        return Notification.builder()
                .userId(sellerId)
                .type(type)
                .content(Map.of(
                        "requestedAt", payload.requestedAt(),
                        "productId", payload.productId(),
                        "productName", payload.productName(),
                        "productSize", payload.productSize(),
                        "price", payload.price(),
                        "productNumber", payload.productNumber()
                ))
                .deepLink("/biddings/" + payload.productId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toSettlementCompletedNotification(Type type, SettlementCompletedPayload payload, Long sellerId) {
        return Notification.builder()
                .userId(sellerId)
                .type(type)
                .content(Map.of(
                        "startAt", payload.startAt(),
                        "endAt", payload.endAt(),
                        "totalNetAmount", payload.totalNetAmount()
                ))
                .deepLink("/settlements/" + payload.sellerId())     // Todo : 실제 딥링크로 수정
                .isRead(false)
                .build();
    }

    public Notification toPriceDroppedNotification(Type type, SellBiddingCreatedPayload payload, Long userId) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .content(Map.of(
                        "targetPrice", payload.currentPrice(),
                        "productId", payload.productId(),
                        "productName", payload.productName(),
                        "productSize", payload.productOption(),
                        "thumbnailImage", payload.thumbnailImage(),
                        "brandName", payload.brandName()
                ))
                .deepLink("/products/" + payload.productId())     // Todo : 실제 딥링크로 수정
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
