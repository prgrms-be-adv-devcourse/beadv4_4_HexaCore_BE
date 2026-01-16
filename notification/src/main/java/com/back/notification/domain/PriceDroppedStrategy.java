package com.back.notification.domain;

import com.back.common.market.event.PriceDroppedEvent;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceDroppedStrategy implements NotificationStrategy<PriceDroppedEvent>{
    private final NotificationMapper mapper;

    public Type type(){
        return Type.PRICE_DROPPED;
    }

    public List<Notification> create(PriceDroppedEvent event){
        return List.of(
                mapper.toPriceDroppedNotification(type(), event, findTarget(event))
        );
    }

    public Long findTarget(PriceDroppedEvent event) {
        return event.userId();
    }
}
