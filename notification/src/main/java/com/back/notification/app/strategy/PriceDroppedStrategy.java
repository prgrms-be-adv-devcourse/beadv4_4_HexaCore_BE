package com.back.notification.app.strategy;

import com.back.common.market.event.SellBiddingCreatedEvent;
import com.back.notification.app.NotificationProductUsecase;
import com.back.notification.app.pricealert.PriceAlertSupport;
import com.back.notification.domain.Notification;
import com.back.notification.domain.NotificationProduct;
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
    private final NotificationProductUsecase notificationProductUsecase;

    @Override
    public Type type(){
        return Type.PRICE_DROPPED;
    }

    @Override
    public List<Notification> create(SellBiddingCreatedEvent event){
        NotificationProduct product = notificationProductUsecase
                .findNotificationProductById(event.productId());

        return findTarget(event).stream()
                .map(user -> mapper.toPriceDroppedNotification(type(), event,
                        user.getId(), product))
                .toList();
    }

    public List<NotificationUser> findTarget(SellBiddingCreatedEvent event) {
        return priceAlertSupport.findUsersForPriceDropAlert(event);
    }
}
