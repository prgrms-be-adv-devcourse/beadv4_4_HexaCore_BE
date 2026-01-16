package com.back.notification.mapper;

import com.back.common.market.event.BiddingCompletedEvent;
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
}
