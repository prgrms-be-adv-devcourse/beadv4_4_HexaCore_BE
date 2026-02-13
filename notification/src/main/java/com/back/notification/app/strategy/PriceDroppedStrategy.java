package com.back.notification.app.strategy;

import com.back.notification.app.pricealert.PriceAlertSupport;
import com.back.notification.domain.Notification;
import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.SellBiddingCreatedPayload;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceDroppedStrategy implements NotificationStrategy<SellBiddingCreatedPayload> {
    private final NotificationMapper mapper;
    private final PriceAlertSupport priceAlertSupport;

    @Override
    public Type type(){
        return Type.PRICE_DROPPED;
    }

    @Override
    public List<Notification> create(SellBiddingCreatedPayload payload){
        return findTarget(payload).stream()
                .map(user -> mapper.toPriceDroppedNotification(type(), payload, user.getId()))
                .toList();
    }

    public List<NotificationUser> findTarget(SellBiddingCreatedPayload payload) {
        return priceAlertSupport.findUsersForPriceDropAlert(payload);
    }
}
