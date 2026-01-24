package com.back.notification.app.strategy;

import com.back.common.market.event.SellBiddingCreatedEvent;
import com.back.notification.app.pricealert.PriceAlertSupport;
import com.back.notification.domain.Notification;
import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceDroppedStrategy implements NotificationStrategy<SellBiddingCreatedEvent> {
    private final NotificationMapper mapper;
    private final PriceAlertSupport priceAlertSupport;

    @Override
    public Type type(){
        return Type.PRICE_DROPPED;
    }

    @Override
    public List<Notification> create(SellBiddingCreatedEvent event){
        return findTarget(event).stream()
                .map(user -> mapper.toPriceDroppedNotification(type(), event, user.getId()))
                .toList();
    }

    public List<NotificationUser> findTarget(SellBiddingCreatedEvent event) {
        return priceAlertSupport.findUsersForPriceDropAlert(event);
    }
}
